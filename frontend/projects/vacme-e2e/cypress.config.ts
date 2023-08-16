import {defineConfig} from 'cypress';

export default defineConfig({
    e2e: {
        setupNodeEvents(on, config) {
            // Add plugin import here 👇
            require('@deploysentinel/cypress-recorder')(on, config);
        },
    },
});
