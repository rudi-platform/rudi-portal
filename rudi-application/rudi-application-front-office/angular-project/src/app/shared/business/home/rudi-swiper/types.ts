import {SwiperOptions} from 'swiper/types';

export interface SwiperBreakpoint {
    [width: number]: SwiperOptions;

    [ratio: string]: SwiperOptions;
}
