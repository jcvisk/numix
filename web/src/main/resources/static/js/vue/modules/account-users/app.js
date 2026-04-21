import { createAccountUsersService } from './service.js';
import { loadI18n, translate } from '../../shared/i18n.js';
import { validateWithSchema, z } from '../../shared/validation.js';

const { createApp, ref, onMounted } = Vue;

function normalizeRole(role) {
    if (typeof role === 'string') {
        return role;
    }
    return role && role.name ? role.name : '';
}

function normalizeUsers(items) {
    return (items || []).map((user) => {
        const roleCode = normalizeRole(user.roleCode);
        const enabled = !!user.enabled;
        return {
            id: user.id,
            fullName: user.fullName,
            email: user.email,
            roleCode,
            enabledString: enabled ? 'true' : 'false'
        };
    });
}

function normalizeRoles(items) {
    return (items || []).map(normalizeRole);
}

createApp({
    setup() {
        const service = createAccountUsersService();
        const loading = ref(false);
        const i18n = ref({});
        const users = ref([]);
        const roles = ref([]);
        const errorMessage = ref('');
        const successMessage = ref('');
        const createFormErrors = ref({});
        const createForm = ref({
            fullName: '',
            email: '',
            password: '',
            roleCode: ''
        });

        const applyData = (data) => {
            users.value = normalizeUsers(data.users);
            roles.value = normalizeRoles(data.roles);
            if (!createForm.value.roleCode && roles.value.length > 0) {
                createForm.value.roleCode = roles.value[0];
            }
        };

        const clearMessages = () => {
            errorMessage.value = '';
            successMessage.value = '';
        };

        const clearCreateFormErrors = () => {
            createFormErrors.value = {};
        };

        const t = (key, fallback) => translate(i18n.value, key, fallback);

        const loadMessages = async () => {
            i18n.value = await loadI18n();
        };

        const createFormSchema = () => z.object({
            fullName: z.string().trim().min(
                1,
                t('usuarios.validation.fullName.required', 'El nombre es obligatorio')
            ),
            email: z.string().trim().min(
                1,
                t('usuarios.validation.email.required', 'El correo es obligatorio')
            ).email(
                t('usuarios.validation.email.invalid', 'El correo no es válido')
            ),
            password: z.string().min(
                1,
                t('usuarios.validation.password.required', 'La contraseña es obligatoria')
            ).min(
                8,
                t('usuarios.validation.password.min', 'La contraseña debe tener al menos 8 caracteres')
            ),
            roleCode: z.string().trim().min(
                1,
                t('usuarios.validation.role.required', 'El rol es obligatorio')
            )
        });

        const validateCreateForm = async () => {
            const result = await validateWithSchema(createFormSchema(), createForm.value);
            createFormErrors.value = result.errors;
            return result.valid;
        };

        const updateFormSchema = () => z.object({
            targetUserId: z.number(),
            roleCode: z.string().trim().min(
                1,
                t('usuarios.validation.role.required', 'El rol es obligatorio')
            ),
            enabled: z.boolean()
        });

        const validateUpdatePayload = async (payload) => {
            const result = await validateWithSchema(updateFormSchema(), payload);
            return result.valid;
        };

        const fetchData = () => {
            loading.value = true;
            return service.fetchData()
                .then((data) => {
                    applyData(data);
                })
                .catch(() => {
                    errorMessage.value = t('usuarios.error.cargar', 'No fue posible cargar los usuarios');
                })
                .finally(() => {
                    loading.value = false;
                });
        };

        const submitCreate = async () => {
            clearMessages();
            clearCreateFormErrors();

            const isValid = await validateCreateForm();
            if (!isValid) {
                errorMessage.value = Object.values(createFormErrors.value)[0]
                    || t('usuarios.validation.form.invalid', 'Revisa los campos del formulario');
                return;
            }

            loading.value = true;

            service.createUser({
                fullName: createForm.value.fullName,
                email: createForm.value.email,
                password: createForm.value.password,
                roleCode: createForm.value.roleCode
            }).then((result) => {
                if (!result.success) {
                    errorMessage.value = result.message || t('usuarios.error.crear', 'No fue posible crear el usuario');
                    if (result.data) {
                        applyData(result.data);
                    }
                    return;
                }

                successMessage.value = result.message;
                if (result.data) {
                    applyData(result.data);
                }
                createForm.value.fullName = '';
                createForm.value.email = '';
                createForm.value.password = '';
                clearCreateFormErrors();
            }).finally(() => {
                loading.value = false;
            });
        };

        const submitUpdate = async (user) => {
            clearMessages();

            const payload = {
                targetUserId: user.id,
                roleCode: user.roleCode,
                enabled: user.enabledString === 'true'
            };
            const isValid = await validateUpdatePayload(payload);
            if (!isValid) {
                errorMessage.value = t('usuarios.validation.form.invalid', 'Revisa los campos del formulario');
                return;
            }

            loading.value = true;

            service.updateUser(payload).then((result) => {
                if (!result.success) {
                    errorMessage.value = result.message || t('usuarios.error.actualizar', 'No fue posible actualizar el usuario');
                    if (result.data) {
                        applyData(result.data);
                    }
                    return;
                }

                successMessage.value = result.message;
                if (result.data) {
                    applyData(result.data);
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
            users,
            roles,
            errorMessage,
            successMessage,
            createFormErrors,
            createForm,
            submitCreate,
            submitUpdate
        };
    }
}).mount('#app');
