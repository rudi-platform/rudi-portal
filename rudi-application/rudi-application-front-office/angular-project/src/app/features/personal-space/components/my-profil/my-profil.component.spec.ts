import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UserService} from '@core/services/user.service';
import {User} from 'micro_service_modules/acl/acl-model';
import {of} from 'rxjs';
import {MyProfilComponent} from './my-profil.component';

describe('MyProfilComponent', () => {
    let fixture: ComponentFixture<MyProfilComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MyProfilComponent],
            providers: [{
                provide: UserService,
                useValue: {
                    getAuthenticatedUser: () => of({firstname: 'Jane', lastname: 'Doe'} as User),
                    lookupEMailAddress: () => 'jane.doe@example.org'
                }
            }]
        }).compileComponents();

        fixture = TestBed.createComponent(MyProfilComponent);
        fixture.detectChanges();
    });

    it('should render the block title as a level 2 heading (RGAA 9.1)', () => {
        expect(fixture.nativeElement.querySelector('h2.my-profil-title')).not.toBeNull();
    });

    it('should render the profile as a description list (RGAA 9.3)', () => {
        const list: HTMLElement = fixture.nativeElement.querySelector('dl.my-profil-card-content');

        expect(list.querySelectorAll('dt').length).toBe(3);
        expect(list.querySelectorAll('dd').length).toBe(3);
        expect(list.querySelectorAll('dd')[2].textContent).toContain('jane.doe@example.org');
    });
});
