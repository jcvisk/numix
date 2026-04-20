import { createDashboardService } from './service.js';
import { loadI18n, translate } from '../../shared/i18n.js';

const { createApp, ref, computed, onMounted } = Vue;

createApp({
    setup() {
        const service = createDashboardService();
        const loading = ref(false);
        const i18n = ref({});
        const errorMessage = ref('');
        const generatedAt = ref('');
        const cards = ref([]);
        const activity = ref([]);

        const t = (key, fallback) => translate(i18n.value, key, fallback);

        const loadMessages = async () => {
            i18n.value = await loadI18n();
        };

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
                    errorMessage.value = t('dashboard.error.cargar', 'No fue posible cargar el dashboard');
                })
                .finally(() => {
                    loading.value = false;
                });
        };

        onMounted(async () => {
            try {
                await loadMessages();
            } finally {
                await fetchSummary();
            }
        });

        return {
            i18n,
            loading,
            errorMessage,
            cards,
            activity,
            todayLabel
        };
    }
}).mount('#app');
