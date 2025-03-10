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

package org.comixedproject.controller.file;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.IOException;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.comixedproject.adaptors.archive.ArchiveAdaptorException;
import org.comixedproject.auditlog.AuditableEndpoint;
import org.comixedproject.handlers.ComicFileHandlerException;
import org.comixedproject.model.net.GetAllComicsUnderRequest;
import org.comixedproject.model.net.ImportComicFilesRequest;
import org.comixedproject.model.net.comicfiles.LoadComicFilesResponse;
import org.comixedproject.service.file.FileService;
import org.comixedproject.task.QueueComicsTask;
import org.comixedproject.task.runner.TaskManager;
import org.comixedproject.views.View;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <code>FileController</code> allows the remote agent to query directories and import files, to
 * download files and work with the file system.
 *
 * @author Darryl L. Pierce
 */
@RestController
@RequestMapping("/api/files")
@Log4j2
public class FileController {
  @Autowired private FileService fileService;
  @Autowired private TaskManager taskManager;
  @Autowired private ObjectFactory<QueueComicsTask> queueComicsWorkerTaskObjectFactory;

  private int requestId = 0;

  /**
   * Retrieves all comic files under the specified directory.
   *
   * @param request the request body
   * @return the list of comic files
   * @throws IOException if an error occurs
   * @throws IOException if an error occurs
   */
  @PostMapping(
      value = "/contents",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @JsonView(View.ComicFileList.class)
  @AuditableEndpoint
  public LoadComicFilesResponse loadComicFiles(
      @RequestBody() final GetAllComicsUnderRequest request) throws IOException {
    String directory = request.getDirectory();
    Integer maximum = request.getMaximum();

    log.info(
        "Getting all comic files: root={} maximum={}",
        directory,
        maximum > 0 ? maximum : "UNLIMITED");

    return new LoadComicFilesResponse(this.fileService.getAllComicsUnder(directory, maximum));
  }

  /**
   * Returns the content for the first image in the specified file.
   *
   * @param filename the file
   * @return the image content, or null
   */
  @GetMapping(value = "/import/cover")
  @AuditableEndpoint
  public byte[] getImportFileCover(@RequestParam("filename") String filename) {
    // for some reason, during development, this value ALWAYS had a trailing
    // space...
    filename = filename.trim();

    log.info("Getting cover image for archive: filename={}", filename);

    byte[] result = null;

    try {
      result = this.fileService.getImportFileCover(filename);
    } catch (ComicFileHandlerException | ArchiveAdaptorException error) {
      log.error("Failed to load cover from import file", error);
    }

    if (result == null) {
      try {
        result = IOUtils.toByteArray(this.getClass().getResourceAsStream("/images/missing.png"));
      } catch (IOException error) {
        log.error("Failed to load the missing page image", error);
      }
    }

    return result;
  }

  /**
   * Begins the process of enqueueing comic files for import
   *
   * @param request the request body
   */
  @PostMapping(
      value = "/import",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @AuditableEndpoint
  public void importComicFiles(@RequestBody() ImportComicFilesRequest request) {
    final List<String> filenames = request.getFilenames();
    final boolean deleteBlockedPages = request.isDeleteBlockedPages();
    final boolean ignoreMetadata = request.isIgnoreMetadata();

    log.info(
        "Importing {} comic files: delete blocked pages={} ignore metadata={}",
        filenames.size(),
        deleteBlockedPages,
        ignoreMetadata);

    final QueueComicsTask task = this.queueComicsWorkerTaskObjectFactory.getObject();
    task.setFilenames(filenames);
    task.setDeleteBlockedPages(deleteBlockedPages);
    task.setIgnoreMetadata(ignoreMetadata);

    log.debug("Enqueueing task");
    this.taskManager.runTask(task);
  }
}
