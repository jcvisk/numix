import { createAccountUsersService } from './service.js';

const rootEl = document.getElementById('users-page');
if (!rootEl) {
    throw new Error('Users root element not found');
}

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

function readInitialMessage(value) {
    if (!value || value === 'null') {
        return '';
    }
    return value;
}

createApp({
    setup() {
        const service = createAccountUsersService(rootEl);
        const loading = ref(false);
        const users = ref([]);
        const roles = ref([]);
        const errorMessage = ref(readInitialMessage(rootEl.dataset.initialError));
        const successMessage = ref(readInitialMessage(rootEl.dataset.initialSuccess));
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

        const fetchData = () => {
            loading.value = true;
            return service.fetchData()
                .then((data) => {
                    applyData(data);
                })
                .catch(() => {
                    errorMessage.value = 'No fue posible cargar los usuarios';
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
                    errorMessage.value = result.message || 'No fue posible crear el usuario';
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
                    errorMessage.value = result.message || 'No fue posible actualizar el usuario';
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

        onMounted(fetchData);

        return {
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
}).mount(rootEl);
