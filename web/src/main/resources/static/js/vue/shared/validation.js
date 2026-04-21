import { toTypedSchema } from '../../../vue/vee-validate-zod.js';
import { z } from '../../../vue/zod.js';

export { z };

export async function validateWithSchema(zodSchema, values) {
    const typedSchema = toTypedSchema(zodSchema);
    const parsed = await typedSchema.parse(values);

    if (!parsed.errors || parsed.errors.length === 0) {
        return {
            valid: true,
            value: parsed.value,
            errors: {}
        };
    }

    const errors = {};
    parsed.errors.forEach((errorItem) => {
        if (!errorItem || !errorItem.path || !errorItem.errors || errorItem.errors.length === 0) {
            return;
        }

        if (!errors[errorItem.path]) {
            errors[errorItem.path] = errorItem.errors[0];
        }
    });

    return {
        valid: false,
        value: values,
        errors
    };
}
