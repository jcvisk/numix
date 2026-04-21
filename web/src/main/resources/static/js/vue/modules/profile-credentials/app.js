import { createProfileCredentialsService } from './service.js';
import { loadI18n, translate } from '../../shared/i18n.js';
import { validateWithSchema, z } from '../../shared/validation.js';

const credentialsEl = document.getElementById('credentials-page');
if (!credentialsEl) {
    throw new Error('Credentials root element not found');
}

const { createApp, ref, computed, onMounted } = Vue;

createApp({
    setup() {
        const service = createProfileCredentialsService();
        const loading = ref(false);
        const i18n = ref({});
        const currentEmail = ref(credentialsEl.dataset.currentEmail || '');
        const errorMessage = ref('');
        const successMessage = ref('');
        const formErrors = ref({});
        const form = ref({
            currentPassword: '',
            newEmail: currentEmail.value,
            confirmEmail: currentEmail.value,
            newPassword: '',
            confirmPassword: ''
        });

        const avatarLetter = computed(() => {
            if (!currentEmail.value) {
                return 'U';
            }
            return currentEmail.value.charAt(0).toUpperCase();
        });

        const clearMessages = () => {
            errorMessage.value = '';
            successMessage.value = '';
        };

        const clearFormErrors = () => {
            formErrors.value = {};
        };

        const t = (key, fallback) => translate(i18n.value, key, fallback);

        const loadMessages = async () => {
            i18n.value = await loadI18n();
        };

        const credentialsSchema = () => z.object({
            currentPassword: z.string().min(
                1,
                t('credenciales.validation.currentPassword.required', 'La contraseña actual es obligatoria')
            ),
            newEmail: z.string().trim().min(
                1,
                t('credenciales.validation.newEmail.required', 'El nuevo correo es obligatorio')
            ).email(
                t('credenciales.validation.newEmail.invalid', 'El correo no es válido')
            ),
            confirmEmail: z.string().trim().min(
                1,
                t('credenciales.validation.confirmEmail.required', 'Debes confirmar el correo')
            ),
            newPassword: z.string().min(
                1,
                t('credenciales.validation.newPassword.required', 'La nueva contraseña es obligatoria')
            ).min(
                8,
                t('credenciales.validation.newPassword.min', 'La nueva contraseña debe tener al menos 8 caracteres')
            ),
            confirmPassword: z.string().min(
                1,
                t('credenciales.validation.confirmPassword.required', 'Debes confirmar la contraseña')
            )
        }).superRefine((values, ctx) => {
            if (values.newEmail !== values.confirmEmail) {
                ctx.addIssue({
                    code: z.ZodIssueCode.custom,
                    path: ['confirmEmail'],
                    message: t('credenciales.validation.confirmEmail.match', 'Los correos no coinciden')
                });
            }

            if (values.newPassword !== values.confirmPassword) {
                ctx.addIssue({
                    code: z.ZodIssueCode.custom,
                    path: ['confirmPassword'],
                    message: t('credenciales.validation.confirmPassword.match', 'Las contraseñas no coinciden')
                });
            }
        });

        const validateCredentialsForm = async () => {
            const result = await validateWithSchema(credentialsSchema(), form.value);
            formErrors.value = result.errors;
            return result.valid;
        };

        const fetchData = () => {
            loading.value = true;
            return service.fetchData()
                .then((data) => {
                    currentEmail.value = data.currentEmail || currentEmail.value;
                    form.value.newEmail = currentEmail.value;
                    form.value.confirmEmail = currentEmail.value;
                })
                .catch(() => {
                    errorMessage.value = t('credenciales.error.cargar', 'No fue posible cargar tus credenciales');
                })
                .finally(() => {
                    loading.value = false;
                });
        };

        const submitCredentials = async () => {
            clearMessages();
            clearFormErrors();

            const isValid = await validateCredentialsForm();
            if (!isValid) {
                errorMessage.value = Object.values(formErrors.value)[0]
                    || t('credenciales.validation.form.invalid', 'Revisa los campos del formulario');
                return;
            }

            loading.value = true;

            service.changeCredentials({
                currentPassword: form.value.currentPassword,
                newEmail: form.value.newEmail,
                confirmEmail: form.value.confirmEmail,
                newPassword: form.value.newPassword,
                confirmPassword: form.value.confirmPassword
            }).then((result) => {
                if (!result.success) {
                    errorMessage.value = result.message || t('credenciales.error.actualizar', 'No fue posible actualizar tus credenciales');
                    return;
                }

                successMessage.value = result.message;
                if (result.redirectUrl) {
                    window.location.href = result.redirectUrl;
                }
            }).finally(() => {
                loading.value = false;
            });
        };

        onMounted(async () => {
            try {
                await loadMessages();
            } finally {
                await fetchData();
            }
        });

        return {
            i18n,
            loading,
            currentEmail,
            avatarLetter,
            errorMessage,
            successMessage,
            formErrors,
            form,
            submitCredentials
        };
    }
}).mount('#app');
