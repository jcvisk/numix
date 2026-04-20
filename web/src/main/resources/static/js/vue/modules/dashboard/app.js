import { createDashboardService } from './service.js';

const rootEl = document.getElementById('dashboard-page');
if (!rootEl) {
    throw new Error('Dashboard root element not found');
}

const { createApp, ref, computed, onMounted } = Vue;

createApp({
    setup() {
        const service = createDashboardService(rootEl);
        const loading = ref(false);
        const errorMessage = ref('');
        const generatedAt = ref('');
        const cards = ref([]);
        const activity = ref([]);

        const todayLabel = computed(() => {
            if (!generatedAt.value) {
                return '';
            }

            const date = new Date(generatedAt.value);
            if (Number.isNaN(date.getTime())) {
                return '';
            }

            return date.toLocaleString('es-MX', {
                dateStyle: 'full',
                timeStyle: 'short'
            });
        });

        const fetchSummary = () => {
            loading.value = true;
            errorMessage.value = '';

            service.fetchSummary()
                .then((summary) => {
                    generatedAt.value = summary.generatedAt || '';
                    cards.value = summary.cards || [];
                    activity.value = summary.activity || [];
                })
                .catch(() => {
                    errorMessage.value = 'No fue posible cargar el dashboard';
                })
                .finally(() => {
                    loading.value = false;
                });
        };

        onMounted(fetchSummary);

        return {
            loading,
            errorMessage,
            cards,
            activity,
            todayLabel
        };
    }
}).mount(rootEl);
