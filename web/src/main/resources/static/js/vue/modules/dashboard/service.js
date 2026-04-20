import { axiosGetJson } from '../../shared/http.js';

const DASHBOARD_SUMMARY_ENDPOINT = '/dashboard/api/summary';

export function createDashboardService() {
    return {
        fetchSummary() {
            return axiosGetJson(DASHBOARD_SUMMARY_ENDPOINT);
        }
    };
}
