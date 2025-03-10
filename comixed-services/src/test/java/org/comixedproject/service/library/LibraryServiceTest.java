/*
 * ComiXed - A digital comic book library management application.
 * Copyright (C) 2020, The ComiXed Project.
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

package org.comixedproject.service.library;

import static junit.framework.TestCase.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang.RandomStringUtils;
import org.comixedproject.model.comic.Comic;
import org.comixedproject.repositories.comic.ComicRepository;
import org.comixedproject.service.comic.PageCacheService;
import org.comixedproject.utils.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LibraryServiceTest {
  private static final Random RANDOM = new Random();
  private static final String TEST_FILENAME = "/home/comixeduser/Library/comicfile.cbz";
  private static final String TEST_IMAGE_CACHE_DIRECTORY =
      "/home/ComiXedReader/.comixed/image-cache";

  @InjectMocks private LibraryService libraryService;
  @Mock private ComicRepository comicRepository;
  @Mock private Comic comic;
  @Mock private File file;
  @Mock private Utils utils;
  @Mock private PageCacheService pageCacheService;

  private List<Comic> comicList = new ArrayList<>();
  private Comic comic1 = new Comic();
  private Comic comic2 = new Comic();
  private Comic comic3 = new Comic();
  private Comic comic4 = new Comic();
  private List<Long> comicIdList = new ArrayList<>();

  @Before
  public void setUp() {
    // updated now
    comic1.setId(1L);
    comic1.setFilename(RandomStringUtils.random(128));
    // updated yesterday
    comic2.setId(2L);
    comic2.setFilename(RandomStringUtils.random(128));
    // updated same as previous comic
    comic3.setId(3L);
    comic3.setFilename(RandomStringUtils.random(128));
    comic4.setId(4L);
    comic4.setFilename(RandomStringUtils.random(128));
  }

  @Test
  public void testConsolidateLibraryNoDeleteFile() {
    Mockito.when(comicRepository.findAllMarkedForDeletion()).thenReturn(comicList);

    List<Comic> result = libraryService.consolidateLibrary(false);

    assertNotNull(result);
    assertSame(comicList, result);

    Mockito.verify(comicRepository, Mockito.times(comicList.size())).delete(comic);
  }

  @Test
  public void testConsolidateLibraryDeleteFile() throws Exception {
    for (int index = 0; index < 25; index++) {
      comicList.add(comic);
    }

    Mockito.when(comicRepository.findAllMarkedForDeletion()).thenReturn(comicList);
    Mockito.doNothing().when(comicRepository).delete(Mockito.any(Comic.class));
    Mockito.when(comic.getFilename()).thenReturn(TEST_FILENAME);
    Mockito.when(comic.getFile()).thenReturn(file);
    Mockito.when(utils.deleteFile(Mockito.any(File.class))).thenReturn(true);

    List<Comic> result = libraryService.consolidateLibrary(true);

    assertNotNull(result);
    assertSame(comicList, result);

    Mockito.verify(comicRepository, Mockito.times(comicList.size())).delete(comic);
    Mockito.verify(comic, Mockito.times(comicList.size())).getFilename();
    Mockito.verify(comic, Mockito.times(comicList.size())).getFile();
    Mockito.verify(utils, Mockito.times(comicList.size())).deleteFile(file);
  }

  @Test
  public void testClearImageCache() throws LibraryException, IOException {
    Mockito.when(pageCacheService.getRootDirectory()).thenReturn(TEST_IMAGE_CACHE_DIRECTORY);
    Mockito.doNothing().when(utils).deleteDirectoryContents(Mockito.anyString());

    libraryService.clearImageCache();

    Mockito.verify(pageCacheService, Mockito.times(1)).getRootDirectory();
    Mockito.verify(utils, Mockito.times(1)).deleteDirectoryContents(TEST_IMAGE_CACHE_DIRECTORY);
  }

  @Test(expected = LibraryException.class)
  public void testClearImageCacheError() throws LibraryException, IOException {
    Mockito.when(pageCacheService.getRootDirectory()).thenReturn(TEST_IMAGE_CACHE_DIRECTORY);
    Mockito.doThrow(IOException.class).when(utils).deleteDirectoryContents(Mockito.anyString());

    libraryService.clearImageCache();

    Mockito.verify(pageCacheService, Mockito.times(1)).getRootDirectory();
    Mockito.verify(utils, Mockito.times(1)).deleteDirectoryContents(TEST_IMAGE_CACHE_DIRECTORY);
  }
}
