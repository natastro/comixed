/*
 * ComiXed - A digital comic book library management application.
 * Copyright (C) 2021, The ComiXed Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses>
 */

import { createAction, props } from '@ngrx/store';
import { BlockedPage } from '@app/blocked-pages/models/blocked-page';

export const loadBlockedPageList = createAction(
  '[Blocked Page List] Loads the blocked page list'
);

export const blockedPageListLoaded = createAction(
  '[Blocked Page List] Blocked page list loaded',
  props<{ entries: BlockedPage[] }>()
);

export const loadBlockedPageListFailed = createAction(
  '[Blocked Page List] Loading the blocked page list failed'
);

export const blockedPageListUpdated = createAction(
  '[Blocked Page List] An updated entry was received',
  props<{ entry: BlockedPage }>()
);

export const blockedPageListRemoval = createAction(
  '[Blocked Page List] A blocked page was removed',
  props<{ entry: BlockedPage }>()
);
