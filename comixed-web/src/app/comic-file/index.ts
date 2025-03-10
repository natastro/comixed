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

import { Params } from '@angular/router';
import { routerReducer, RouterReducerState } from '@ngrx/router-store';
import { ActionReducerMap } from '@ngrx/store';
import {
  COMIC_IMPORT_FEATURE_KEY,
  ComicImportState,
  reducer as comicImportReducer
} from './reducers/comic-import.reducer';
import {
  COMIC_FILE_LIST_FEATURE_KEY,
  ComicFileListState,
  reducer as comicFileListReducer
} from '@app/comic-file/reducers/comic-file-list.reducer';
import {
  IMPORT_COMIC_FILES_FEATURE_KEY,
  ImportComicFilesState,
  reducer as importComicFilesReducer
} from '@app/comic-file/reducers/import-comic-files.reducer';

interface RouterStateUrl {
  url: string;
  params: Params;
  queryParams: Params;
}

export interface ImportingModuleState {
  router: RouterReducerState<RouterStateUrl>;
  [COMIC_FILE_LIST_FEATURE_KEY]: ComicFileListState;
  [IMPORT_COMIC_FILES_FEATURE_KEY]: ImportComicFilesState;
  [COMIC_IMPORT_FEATURE_KEY]: ComicImportState;
}

export type ModuleState = ImportingModuleState;

export const reducers: ActionReducerMap<ImportingModuleState> = {
  router: routerReducer,
  [COMIC_IMPORT_FEATURE_KEY]: comicImportReducer,
  [COMIC_FILE_LIST_FEATURE_KEY]: comicFileListReducer,
  [IMPORT_COMIC_FILES_FEATURE_KEY]: importComicFilesReducer
};
