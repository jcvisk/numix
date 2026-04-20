import { createAccountUsersService } from './service.js';
import { loadI18n, translate } from '../../shared/i18n.js';

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

        const t = (key, fallback) => translate(i18n.value, key, fallback);

        const loadMessages = async () => {
            i18n.value = await loadI18n();
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

        const submitCreate = () => {
            clearMessages();
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
            }).finally(() => {
                loading.value = false;
            });
        };

        const submitUpdate = (user) => {
            clearMessages();
            loading.value = true;

            service.updateUser({
                targetUserId: user.id,
                roleCode: user.roleCode,
                enabled: user.enabledString === 'true'
            }).then((result) => {
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
            createForm,
            submitCreate,
            submitUpdate
        };
    }
}).mount('#app');
