/*
 * ComiXed - A digital comic book library management application.
 * Copyright (C) 2017, The ComiXed Project
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

package org.comixedproject.task;

import java.io.File;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.comixedproject.adaptors.FilenameScraperAdaptor;
import org.comixedproject.handlers.ComicFileHandler;
import org.comixedproject.messaging.comic.PublishComicUpdateAction;
import org.comixedproject.model.comic.Comic;
import org.comixedproject.model.comic.ComicState;
import org.comixedproject.model.tasks.PersistedTask;
import org.comixedproject.model.tasks.PersistedTaskType;
import org.comixedproject.service.comic.ComicService;
import org.comixedproject.service.task.TaskService;
import org.comixedproject.task.encoders.ProcessComicTaskEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <code>AddComicTask</code> handles adding a comic to the library.
 *
 * @author Darryl L. Pierce
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ConfigurationProperties(prefix = "comic-file.handlers")
@Log4j2
public class AddComicTask extends AbstractTask {
  @Autowired private ObjectFactory<Comic> comicFactory;
  @Autowired private ComicFileHandler comicFileHandler;
  @Autowired private ComicService comicService;
  @Autowired private FilenameScraperAdaptor filenameScraper;
  @Autowired private ObjectFactory<ProcessComicTaskEncoder> processTaskEncoderObjectFactory;
  @Autowired private TaskService taskService;
  @Autowired private PublishComicUpdateAction publishComicUpdateAction;
  @Getter @Setter private String filename;
  @Getter @Setter private boolean deleteBlockedPages = false;
  @Getter @Setter private boolean ignoreMetadata = false;

  public AddComicTask() {
    super(PersistedTaskType.ADD_COMIC);
  }

  @Override
  @Transactional
  public void startTask() throws TaskException {
    log.debug("Adding file to library: {}", this.filename);

    final File file = new File(this.filename);
    Comic result = this.comicService.findByFilename(file.getAbsolutePath());

    if (result != null) {
      log.debug("Comic already imported: " + file.getAbsolutePath());
      return;
    }

    try {
      result = this.comicFactory.getObject();
      log.debug("Setting comic filename");
      result.setFilename(file.getAbsolutePath());
      log.debug("Scraping details from filename");
      this.filenameScraper.execute(result);
      log.debug("Loading comic details");
      this.comicFileHandler.loadComicArchiveType(result);

      log.debug("Saving comic");
      result.setComicState(ComicState.ADDED);
      result = this.comicService.save(result);

      log.trace("Publishing updated comic");
      this.publishComicUpdateAction.publish(result);

      log.debug("Encoding process comic persistedTask");
      final ProcessComicTaskEncoder taskEncoder = this.processTaskEncoderObjectFactory.getObject();
      taskEncoder.setComic(result);
      taskEncoder.setDeleteBlockedPages(this.deleteBlockedPages);
      taskEncoder.setIgnoreMetadata(this.ignoreMetadata);

      log.debug("Saving process comic persistedTask");
      final PersistedTask persistedTask = taskEncoder.encode();
      this.taskService.save(persistedTask);
    } catch (Exception error) {
      throw new TaskException("Failed to load comic", error);
    }
  }

  @Override
  protected String createDescription() {
    final StringBuilder result = new StringBuilder();

    result
        .append("Add comic to library:")
        .append(" filename=")
        .append(this.filename)
        .append(" delete blocked pages=")
        .append(this.deleteBlockedPages ? "Yes" : "No")
        .append(" ignore metadata=")
        .append(this.ignoreMetadata ? "Yes" : "No");

    return result.toString();
  }
}
