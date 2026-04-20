import { axiosGetJson, axiosPostJson, resolveApiErrorMessage } from '../../shared/http.js';

const USERS_DATA_ENDPOINT = '/account/users/api/data';
const CREATE_USER_ENDPOINT = '/account/users/api/create';
const UPDATE_USER_ENDPOINT = '/account/users/api/update';

export function createAccountUsersService() {
    return {
        fetchData() {
            return axiosGetJson(USERS_DATA_ENDPOINT);
        },
        createUser(payload) {
            return axiosPostJson(CREATE_USER_ENDPOINT, payload)
                .catch((error) => ({
                    success: false,
                    message: resolveApiErrorMessage(error)
                }));
        },
        updateUser(payload) {
            return axiosPostJson(UPDATE_USER_ENDPOINT, payload)
                .catch((error) => ({
                    success: false,
                    message: resolveApiErrorMessage(error)
                }));
        }
    };
}
