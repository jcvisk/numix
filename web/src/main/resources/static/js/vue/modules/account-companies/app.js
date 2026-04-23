import { createAccountCompaniesService } from './service.js';
import { loadI18n, translate } from '../../shared/i18n.js';
import { validateWithSchema, z } from '../../shared/validation.js';

const { createApp, ref, computed, onMounted } = Vue;

function normalizeCompanies(items) {
    return (items || []).map((company) => ({
        id: company.id,
        name: company.name,
        legalName: company.legalName,
        taxId: company.taxId,
        fiscalRegime: company.fiscalRegime,
        taxZipCode: company.taxZipCode || '',
        baseCurrencyId: company.baseCurrencyId,
        baseCurrencyCode: company.baseCurrencyCode,
        email: company.email || '',
        phone: company.phone || '',
        address: company.address || '',
        curp: company.curp || '',
        birthDate: company.birthDate || '',
        status: company.status || 'ACTIVE'
    }));
}

function normalizeUsers(items) {
    return (items || []).map((user) => {
        const roleCode = typeof user.roleCode === 'string' ? user.roleCode : (user.roleCode?.name || '');
        return {
            id: user.id,
            fullName: user.fullName,
            email: user.email,
            roleCode
        };
    });
}

createApp({
    setup() {
        const service = createAccountCompaniesService();

        const i18n = ref({});
        const loading = ref(false);
        const errorMessage = ref('');
        const successMessage = ref('');

        const companies = ref([]);
        const currencies = ref([]);
        const users = ref([]);

        const createFormErrors = ref({});
        const createForm = ref({
            name: '',
            legalName: '',
            taxId: '',
            fiscalRegime: '',
            taxZipCode: '',
            baseCurrencyId: null,
            email: '',
            phone: '',
            address: '',
            curp: '',
            birthDate: ''
        });

        const assignmentForm = ref({
            targetUserId: null,
            companyIds: []
        });

        const assignableUsers = computed(() => users.value.filter((user) => user.roleCode === 'ADMIN' || user.roleCode === 'AUX'));

        const t = (key, fallback) => translate(i18n.value, key, fallback);

        const createSchema = () => z.object({
            name: z.string().trim().min(1, t('empresas.validation.name.required', 'El nombre de la empresa es obligatorio')),
            legalName: z.string().trim().min(1, t('empresas.validation.legalName.required', 'La razón social es obligatoria')),
            taxId: z.string().trim().min(1, t('empresas.validation.taxId.required', 'El RFC/Tax ID es obligatorio')),
            fiscalRegime: z.string().trim().min(1, t('empresas.validation.fiscalRegime.required', 'El régimen fiscal es obligatorio')),
            baseCurrencyId: z.number({ invalid_type_error: t('empresas.validation.currency.required', 'La moneda base es obligatoria') }),
            curp: z.string().trim().max(18, t('empresas.validation.curp.max', 'La CURP no puede exceder 18 caracteres')).optional().or(z.literal(''))
        });

        const assignmentSchema = () => z.object({
            targetUserId: z.number(),
            companyIds: z.array(z.number()).min(1, t('empresas.validation.assignment.min', 'Selecciona al menos una empresa'))
        });

        const clearMessages = () => {
            errorMessage.value = '';
            successMessage.value = '';
        };

        const applyCompaniesData = (data) => {
            companies.value = normalizeCompanies(data.companies);
            currencies.value = data.currencies || [];
            if (!createForm.value.baseCurrencyId && currencies.value.length > 0) {
                createForm.value.baseCurrencyId = currencies.value[0].id;
            }
        };

        const applyUsersData = (data) => {
            users.value = normalizeUsers(data.users);
        };

        const fetchAll = async () => {
            loading.value = true;
            try {
                const [companiesData, usersData] = await Promise.all([
                    service.fetchCompaniesData(),
                    service.fetchUsersData()
                ]);
                applyCompaniesData(companiesData);
                applyUsersData(usersData);
            } catch {
                errorMessage.value = t('empresas.error.cargar', 'No fue posible cargar la información de empresas');
            } finally {
                loading.value = false;
            }
        };

        const submitCreate = async () => {
            clearMessages();
            const validation = await validateWithSchema(createSchema(), createForm.value);
            createFormErrors.value = validation.errors;
            if (!validation.valid) {
                errorMessage.value = Object.values(createFormErrors.value)[0] || t('empresas.validation.form.invalid', 'Revisa los datos del formulario');
                return;
            }

            loading.value = true;
            try {
                const result = await service.createCompany({ ...createForm.value });
                if (!result.success) {
                    errorMessage.value = result.message || t('empresas.error.crear', 'No fue posible crear la empresa');
                    if (result.data) {
                        applyCompaniesData(result.data);
                    }
                    return;
                }
                successMessage.value = result.message || 'Empresa creada correctamente';
                if (result.data) {
                    applyCompaniesData(result.data);
                }
                createForm.value = {
                    name: '',
                    legalName: '',
                    taxId: '',
                    fiscalRegime: '',
                    taxZipCode: '',
                    baseCurrencyId: currencies.value.length > 0 ? currencies.value[0].id : null,
                    email: '',
                    phone: '',
                    address: '',
                    curp: '',
                    birthDate: ''
                };
                createFormErrors.value = {};
            } finally {
                loading.value = false;
            }
        };

        const submitUpdate = async (company) => {
            clearMessages();
            loading.value = true;
            try {
                const result = await service.updateCompany({
                    companyId: company.id,
                    name: company.name,
                    legalName: company.legalName,
                    taxId: company.taxId,
                    fiscalRegime: company.fiscalRegime,
                    taxZipCode: company.taxZipCode,
                    baseCurrencyId: company.baseCurrencyId,
                    email: company.email,
                    phone: company.phone,
                    address: company.address,
                    curp: company.curp,
                    birthDate: company.birthDate,
                    status: company.status
                });
                if (!result.success) {
                    errorMessage.value = result.message || t('empresas.error.actualizar', 'No fue posible actualizar la empresa');
                    if (result.data) {
                        applyCompaniesData(result.data);
                    }
                    return;
                }
                successMessage.value = result.message || 'Empresa actualizada correctamente';
                if (result.data) {
                    applyCompaniesData(result.data);
                }
            } finally {
                loading.value = false;
            }
        };

        const submitDelete = async (companyId) => {
            clearMessages();
            loading.value = true;
            try {
                const result = await service.deleteCompany({ companyId });
                if (!result.success) {
                    errorMessage.value = result.message || t('empresas.error.eliminar', 'No fue posible eliminar la empresa');
                    if (result.data) {
                        applyCompaniesData(result.data);
                    }
                    return;
                }
                successMessage.value = result.message || 'Empresa eliminada correctamente';
                if (result.data) {
                    applyCompaniesData(result.data);
                }
            } finally {
                loading.value = false;
            }
        };

        const submitAssignment = async () => {
            clearMessages();
            const payload = {
                targetUserId: assignmentForm.value.targetUserId,
                companyIds: assignmentForm.value.companyIds
            };
            const validation = await validateWithSchema(assignmentSchema(), payload);
            if (!validation.valid) {
                errorMessage.value = Object.values(validation.errors)[0] || t('empresas.validation.assignment.invalid', 'Datos de asignación inválidos');
                return;
            }

            loading.value = true;
            try {
                const result = await service.assignCompanies(payload);
                if (!result.success) {
                    errorMessage.value = result.message || t('empresas.error.asignar', 'No fue posible asignar empresas');
                    if (result.data) {
                        applyCompaniesData(result.data);
                    }
                    return;
                }
                successMessage.value = result.message || 'Empresas asignadas correctamente';
                if (result.data) {
                    applyCompaniesData(result.data);
                }
            } finally {
                loading.value = false;
            }
        };

        onMounted(async () => {
            i18n.value = await loadI18n();
            await fetchAll();
        });

        return {
            i18n,
            loading,
            errorMessage,
            successMessage,
            companies,
            currencies,
            users,
            assignableUsers,
            createForm,
            createFormErrors,
            assignmentForm,
            submitCreate,
            submitUpdate,
            submitDelete,
            submitAssignment
        };
    }
}).mount('#app');
