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

package org.comixedproject.task.encoders;

import lombok.extern.log4j.Log4j2;
import org.comixedproject.model.comic.Comic;
import org.comixedproject.model.tasks.PersistedTask;
import org.comixedproject.model.tasks.PersistedTaskType;
import org.comixedproject.service.task.TaskService;
import org.comixedproject.task.RescanComicTask;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Log4j2
public class RescanComicTaskEncoder extends AbstractTaskEncoder<RescanComicTask> {
  @Autowired private TaskService taskService;
  @Autowired private ObjectFactory<RescanComicTask> rescanComicWorkerTaskObjectFactory;

  private Comic comic;

  public void setComic(final Comic comic) {
    this.comic = comic;
  }

  @Override
  public PersistedTask encode() {
    log.debug("Encoding rescan comic task: comic={}", this.comic.getId());

    final PersistedTask result = new PersistedTask();
    result.setTaskType(PersistedTaskType.RESCAN_COMIC);
    result.setComic(this.comic);
    result.setProperty("DUMMY", "PROPERTY");

    return result;
  }

  @Override
  public RescanComicTask decode(final PersistedTask persistedTask) {
    this.taskService.delete(persistedTask);

    log.debug("Decoding rescan comic worker persistedTask: comic={}", persistedTask.getId());
    final RescanComicTask result = this.rescanComicWorkerTaskObjectFactory.getObject();
    result.setComic(persistedTask.getComic());
    return result;
  }
}
