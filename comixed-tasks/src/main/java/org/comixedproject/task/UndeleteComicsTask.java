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

package org.comixedproject.task;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.comixedproject.model.comic.Comic;
import org.comixedproject.model.tasks.PersistedTask;
import org.comixedproject.model.tasks.PersistedTaskType;
import org.comixedproject.service.comic.ComicException;
import org.comixedproject.service.comic.ComicService;
import org.comixedproject.service.task.TaskService;
import org.comixedproject.task.encoders.UndeleteComicTaskEncoder;
import org.comixedproject.views.View;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <code>UndeleteComicsTask</code> is a task that creates multiple {@link UndeleteComicTask}
 * instances, one for each come to be undeleted.
 *
 * @author Darryl L. Pierce
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Log4j2
public class UndeleteComicsTask extends AbstractTask {
  @Autowired private ObjectFactory<UndeleteComicTaskEncoder> undeleteComicTaskEncoderObjectFactory;
  @Autowired private ComicService comicService;
  @Autowired private TaskService taskService;

  @JsonView(View.AuditLogEntryDetail.class)
  @Getter
  @Setter
  private List<Long> ids = new ArrayList<>();

  public UndeleteComicsTask() {
    super(PersistedTaskType.UNDELETE_COMICS);
  }

  @Override
  protected String createDescription() {
    return String.format("Undeleting %d comic%s", this.ids.size(), this.ids.size() == 1 ? "" : "s");
  }

  @Override
  public void startTask() throws TaskException {
    log.debug(
        "Creating tasks to undelete {} comic{}", this.ids.size(), this.ids.size() == 1 ? "" : "s");
    for (int index = 0; index < this.ids.size(); index++) {
      Long id = ids.get(index);
      Comic comic = null;
      try {
        comic = this.comicService.getComic(id);
      } catch (ComicException error) {
        throw new TaskException("failed to load comic", error);
      }
      log.debug("Creating undelete persistedTask for comic: id={}", comic.getId());
      UndeleteComicTaskEncoder encoder = this.undeleteComicTaskEncoderObjectFactory.getObject();
      encoder.setComic(comic);
      log.debug("enqueueing undelete persistedTask");
      PersistedTask persistedTask = encoder.encode();
      this.taskService.save(persistedTask);
    }
  }
}
