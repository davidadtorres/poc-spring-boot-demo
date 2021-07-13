package com.example.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.demo.exception.ErrorMessage;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Tutorial;
import com.example.demo.model.Info;
import com.example.demo.repository.TutorialRepository;
import com.example.demo.repository.PageTutorialRepository;

import org.springframework.beans.factory.annotation.Value;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

	@Value("${application.name}")
	private String applicationName;

	@Value("${build.version}")
	private String buildVersion;

	@Value("${build.timestamp}")
	private String buildTimestamp;

	@Value("${build}")
	private long build;


	@Autowired
	TutorialRepository tutorialRepository;

	@Operation(summary = "Get API information")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Info found", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = Info.class)) }),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@GetMapping("/info")
	public ResponseEntity<Info> getInfo() {
		Info info = new Info(applicationName, buildVersion, buildTimestamp, build);

		return new ResponseEntity<>(info, HttpStatus.OK);
	}

	@Operation(summary = "Get all tutorials")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Tutorials found", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Tutorial.class))) }),
			@ApiResponse(responseCode = "204", description = "No tutorials", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@GetMapping("/tutorials")
	public ResponseEntity<List<Tutorial>> getAllTutorials(
			@Parameter(description = "Tutorial title to be searched") @RequestParam(required = false) String title) {
		List<Tutorial> tutorials = new ArrayList<Tutorial>();

		if (title == null)
			tutorialRepository.findAll().forEach(tutorials::add);
		else
			tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);

		if (tutorials.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(tutorials, HttpStatus.OK);
	}

	@Operation(summary = "Get a tutorial by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Tutorial found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Tutorial.class)) }),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@GetMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> getTutorialById(
			@Parameter(description = "Tutorial id to be searched") @PathVariable("id") long id) {
		Tutorial tutorial = tutorialRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Not found tutorial with id = " + id));

		return new ResponseEntity<>(tutorial, HttpStatus.OK);
	}

	@Operation(summary = "Create new tutorial")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Tutorial created", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Tutorial.class)) }),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@PostMapping("/tutorials")
	public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
		Tutorial _tutorial = tutorialRepository
				.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false));
		return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
	}

	@Operation(summary = "Update a tutorial")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Tutorial updated", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Tutorial.class)) }),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@PutMapping("/tutorials/{id}")
	public ResponseEntity<Tutorial> updateTutorial(
			@Parameter(description = "Tutorial id to be searched") @PathVariable("id") long id,
			@RequestBody Tutorial tutorial) {
		Tutorial _tutorial = tutorialRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Not found Tutorial with id = " + id));

		_tutorial.setTitle(tutorial.getTitle());
		_tutorial.setDescription(tutorial.getDescription());
		_tutorial.setPublished(tutorial.isPublished());

		return new ResponseEntity<>(tutorialRepository.save(_tutorial), HttpStatus.OK);
	}

	@Operation(summary = "Remove a tutorial")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Tutorial removed", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@DeleteMapping("/tutorials/{id}")
	public ResponseEntity<HttpStatus> deleteTutorial(
			@Parameter(description = "Tutorial id to be searched") @PathVariable("id") long id) {
		tutorialRepository.deleteById(id);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Operation(summary = "Remove all tutorials")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "All tutorials removed", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@DeleteMapping("/tutorials")
	public ResponseEntity<HttpStatus> deleteAllTutorials() {
		tutorialRepository.deleteAll();

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Operation(summary = "Get all tutorials published")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Published tutorials found", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Tutorial.class))) }),
			@ApiResponse(responseCode = "204", description = "No published tutorials", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@GetMapping("/tutorials/published")
	public ResponseEntity<List<Tutorial>> findByPublished() {
		List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

		if (tutorials.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(tutorials, HttpStatus.OK);
	}

	@Autowired
	PageTutorialRepository pageTutorialRepository;

	@Operation(summary = "Get all tutorials paged")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Tutorials found", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Tutorial.class))) }),
			@ApiResponse(responseCode = "204", description = "No tutorials", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@GetMapping("/page/tutorials")
	public ResponseEntity<Map<String, Object>> getAllTutorials(
			@Parameter(description = "Tutorial title to be searched") @RequestParam(required = false) String title,
			@Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "3") int size) {

		List<Tutorial> tutorials = new ArrayList<Tutorial>();
		Pageable paging = PageRequest.of(page, size);

		Page<Tutorial> pageTuts;
		if (title == null)
			pageTuts = pageTutorialRepository.findAll(paging);
		else
			pageTuts = pageTutorialRepository.findByTitleContaining(title, paging);

		tutorials = pageTuts.getContent();

		if (tutorials.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("tutorials", tutorials);
		response.put("currentPage", pageTuts.getNumber());
		response.put("totalItems", pageTuts.getTotalElements());
		response.put("totalPages", pageTuts.getTotalPages());

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Operation(summary = "Get all tutorials paged and published")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Published tutorials found", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Tutorial.class))) }),
			@ApiResponse(responseCode = "204", description = "No published tutorials", content = @Content),
			@ApiResponse(responseCode = "404", description = "Not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)) }) })
	@GetMapping("/page/tutorials/published")
	public ResponseEntity<Map<String, Object>> findByPublished(
			@Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "3") int size) {

		List<Tutorial> tutorials = new ArrayList<Tutorial>();
		Pageable paging = PageRequest.of(page, size);

		Page<Tutorial> pageTuts = pageTutorialRepository.findByPublished(true, paging);
		tutorials = pageTuts.getContent();

		if (tutorials.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("tutorials", tutorials);
		response.put("currentPage", pageTuts.getNumber());
		response.put("totalItems", pageTuts.getTotalElements());
		response.put("totalPages", pageTuts.getTotalPages());

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}