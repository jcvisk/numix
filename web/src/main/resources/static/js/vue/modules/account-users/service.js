export function createAccountUsersService(rootEl) {
    function get(url) {
        return axios.get(url, {
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                Accept: 'application/json'
            }
        });
    }

    function post(url, payload) {
        const headers = {
            'X-Requested-With': 'XMLHttpRequest',
            'Content-Type': 'application/json',
            Accept: 'application/json'
        };

        if (rootEl.dataset.csrfHeader && rootEl.dataset.csrfToken) {
            headers[rootEl.dataset.csrfHeader] = rootEl.dataset.csrfToken;
        }

        return axios.post(url, payload, { headers });
    }

    function resolveErrorMessage(error, fallback) {
        if (error && error.response && error.response.data && error.response.data.message) {
            return error.response.data.message;
        }
        return fallback;
    }

    return {
        fetchData() {
            return get(rootEl.dataset.fetchUrl).then((response) => response.data);
        },
        createUser(payload) {
            return post(rootEl.dataset.createUrl, payload)
                .then((response) => response.data)
                .catch((error) => ({
                    success: false,
                    message: resolveErrorMessage(error, 'No fue posible crear el usuario')
                }));
        },
        updateUser(payload) {
            return post(rootEl.dataset.updateUrl, payload)
                .then((response) => response.data)
                .catch((error) => ({
                    success: false,
                    message: resolveErrorMessage(error, 'No fue posible actualizar el usuario')
                }));
        }
    };
}
