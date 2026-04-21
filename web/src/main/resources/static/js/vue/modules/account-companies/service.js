import { axiosGetJson, axiosPostJson, resolveApiErrorMessage } from '../../shared/http.js';

const COMPANIES_DATA_ENDPOINT = '/account/companies/api/data';
const USERS_DATA_ENDPOINT = '/account/users/api/data';
const CREATE_COMPANY_ENDPOINT = '/account/companies/api/create';
const UPDATE_COMPANY_ENDPOINT = '/account/companies/api/update';
const DELETE_COMPANY_ENDPOINT = '/account/companies/api/delete';
const ASSIGN_COMPANIES_ENDPOINT = '/account/companies/api/assign-user';

function withApiError(promise) {
    return promise.catch((error) => ({
        success: false,
        message: resolveApiErrorMessage(error)
    }));
}

export function createAccountCompaniesService() {
    return {
        fetchCompaniesData() {
            return axiosGetJson(COMPANIES_DATA_ENDPOINT);
        },
        fetchUsersData() {
            return axiosGetJson(USERS_DATA_ENDPOINT);
        },
        createCompany(payload) {
            return withApiError(axiosPostJson(CREATE_COMPANY_ENDPOINT, payload));
        },
        updateCompany(payload) {
            return withApiError(axiosPostJson(UPDATE_COMPANY_ENDPOINT, payload));
        },
        deleteCompany(payload) {
            return withApiError(axiosPostJson(DELETE_COMPANY_ENDPOINT, payload));
        },
        assignCompanies(payload) {
            return withApiError(axiosPostJson(ASSIGN_COMPANIES_ENDPOINT, payload));
        }
    };
}
