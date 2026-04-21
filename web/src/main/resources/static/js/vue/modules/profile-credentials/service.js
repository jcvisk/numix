import { axiosGetJson, axiosPostJson, resolveApiErrorMessage } from '../../shared/http.js';

const CREDENTIALS_DATA_ENDPOINT = '/profile/credentials/api/data';
const CHANGE_CREDENTIALS_ENDPOINT = '/profile/credentials/api/change';

export function createProfileCredentialsService() {
    return {
        fetchData() {
            return axiosGetJson(CREDENTIALS_DATA_ENDPOINT);
        },
        changeCredentials(payload) {
            return axiosPostJson(CHANGE_CREDENTIALS_ENDPOINT, payload)
                .catch((error) => ({
                    success: false,
                    message: resolveApiErrorMessage(error)
                }));
        }
    };
}
