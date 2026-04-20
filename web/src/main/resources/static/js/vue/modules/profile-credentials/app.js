import { createProfileCredentialsService } from './service.js';
import { loadI18n, translate } from '../../shared/i18n.js';

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

        const t = (key, fallback) => translate(i18n.value, key, fallback);

        const loadMessages = async () => {
            i18n.value = await loadI18n();
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

        const submitCredentials = () => {
            clearMessages();
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
            form,
            submitCredentials
        };
    }
}).mount('#app');
