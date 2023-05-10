/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {animate, AnimationTriggerMetadata, state, style, transition, trigger} from '@angular/animations';

export const EXPANSION_PANEL_ANIMATION_TIMING = '225ms cubic-bezier(0.4,0.0,0.2,1)';

export const libExpansionAnimations: {
    readonly elementExpansion: AnimationTriggerMetadata;
    readonly elementFadeIn: AnimationTriggerMetadata;
} = {
    /** Animation that expands and collapses the panel content. */
    elementExpansion: trigger('elementExpansion', [
        state('collapsed, void', style({height: '0px', visibility: 'hidden'})),
        state('expanded', style({height: '*', visibility: 'visible'})),
        transition('expanded <=> collapsed, void => collapsed',
            animate(EXPANSION_PANEL_ANIMATION_TIMING)),
    ]),
    elementFadeIn: trigger('elementFadeIn', [
        transition(':enter', [
            style({opacity: 0}),
            animate(EXPANSION_PANEL_ANIMATION_TIMING, style({opacity: 1}))]),
        transition(':leave', [
            style({opacity: 1}),
            animate(EXPANSION_PANEL_ANIMATION_TIMING, style({opacity: 0}))])
    ])
};
