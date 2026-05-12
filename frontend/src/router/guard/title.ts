import type { Router } from 'vue-router';
import { useTitle } from '@vueuse/core';
import { getLocalizedMetaTitle } from '@/locales';

export function createDocumentTitleGuard(router: Router) {
  router.afterEach(to => {
    useTitle(getLocalizedMetaTitle(to.meta));
  });
}
