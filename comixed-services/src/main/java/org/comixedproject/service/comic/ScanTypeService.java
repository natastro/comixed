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

package org.comixedproject.service.comic;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.comixedproject.model.comic.ScanType;
import org.comixedproject.repositories.comic.ScanTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <code>ScanTypeService</code> provides business logic methods for working with instances of {@link
 * ScanType}.
 *
 * @author Darryl L. Pierce
 */
@Service
@Log4j2
public class ScanTypeService {
  @Autowired private ScanTypeRepository scanTypeRepository;

  /**
   * Returns the list of all defined {@link ScanType}s.
   *
   * @return the list
   */
  public List<ScanType> getAll() {
    log.debug("Getting all scan types");
    return this.scanTypeRepository.findAll();
  }
}
