/*
 * ComiXed - A digital comic book library management application.
 * Copyright (C) 2020, The ComiXed Project
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

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.comixedproject.model.comic.Comic;
import org.comixedproject.model.tasks.PersistedTaskType;
import org.comixedproject.service.comic.ComicService;
import org.comixedproject.service.task.TaskService;
import org.comixedproject.task.encoders.RescanComicTaskEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <code>RescanComicsTask</code> queues instances of {@link RescanComicTask}.
 *
 * @author Darryl L. Pierce
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Log4j2
public class RescanComicsTask extends AbstractTask {
  private static final int MAX_RECORDS = 100;

  @Autowired private ComicService comicService;
  @Autowired private TaskService taskService;
  @Autowired private ObjectFactory<RescanComicTaskEncoder> rescanComicTaskEncoderObjectFactory;

  public RescanComicsTask() {
    super(PersistedTaskType.RESCAN_COMICS);
  }

  @Override
  protected String createDescription() {
    return "Rescanning comics";
  }

  @Override
  public void startTask() {
    log.debug("Rescanning comic library");

    boolean done = false;
    long lastId = 0L;
    int count = 0;

    while (!done) {
      final List<Comic> comics = this.comicService.getComicsById(lastId, MAX_RECORDS);
      if (!comics.isEmpty()) {
        for (Comic comic : comics) {
          final RescanComicTaskEncoder encoder =
              this.rescanComicTaskEncoderObjectFactory.getObject();
          encoder.setComic(comic);
          this.taskService.save(encoder.encode());
          if (comic.getId() > lastId) lastId = comic.getId();
          count++;
        }
      } else {
        log.debug("Started rescan on {} comic{}", count, count == 1 ? "" : "s");
        done = true;
      }
    }
  }
}
