import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';

import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { APP_CONFIG } from './app/app-config'; 

fetch('/assets/config.json')
  .then(res => res.json())
  .then(config => {
    if (config.production) {
      import('@angular/core').then(({ enableProdMode }) => enableProdMode());
    }

    bootstrapApplication(AppComponent, {
      providers: [
        provideRouter(routes),
        provideHttpClient(withFetch()),
        provideAnimations(),
        provideToastr({
          positionClass: 'toast-bottom-right',
          preventDuplicates: true
        }),
        { provide: APP_CONFIG, useValue: config } // ✅ Utilisation correcte du token typé
      ]
    }).catch(err => console.error(err));
  });
