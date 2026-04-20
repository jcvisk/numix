export function createDashboardService(rootEl) {
    return {
        fetchSummary() {
            return axios.get(rootEl.dataset.fetchUrl, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    Accept: 'application/json'
                }
            }).then((response) => response.data);
        }
    };
}
