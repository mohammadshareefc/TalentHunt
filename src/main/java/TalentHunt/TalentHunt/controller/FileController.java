package TalentHunt.TalentHunt.controller;

import TalentHunt.TalentHunt.model.*;
import TalentHunt.TalentHunt.service.*;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
@RequestMapping("/files")
public class FileController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ArticleService articleService;
    @Autowired
    private JobApplicationService jobApplicationService;

    @GetMapping("/profileImage/{userId}")
    public ResponseEntity<ByteArrayResource> serveProfileImage(@PathVariable("userId") Long userId) {
        try {
            User user = userService.getUserById(userId);
            byte[] profileImage = user.getProfileImage();

            if (profileImage == null) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(profileImage);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg"); // Adjust the content type if needed

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(profileImage.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/backgroundImage/{userId}")
    public ResponseEntity<ByteArrayResource> serveBackgroundImage(@PathVariable("userId") Long userId) {
        try {
            TalentHunt.TalentHunt.model.User user = userService.getUserById(userId);
            byte[] backgroundImage = user.getBackgroundImage();

            if (backgroundImage == null) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(backgroundImage);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg"); // Adjust the content type if needed

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(backgroundImage.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/image/{postId}")
    public ResponseEntity<ByteArrayResource> serveImage(@PathVariable("postId") Long postId, HttpServletResponse response) {
        try {
            Post post = postService.getPostById(postId);
            byte[] media = post.getImage();

            if (media == null) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(media);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg"); // Adjust the content type if needed

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(media.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/document/{postId}")
    public ResponseEntity<ByteArrayResource> serveDocument(@PathVariable("postId") Long postId, HttpServletResponse response) {
        try {
            Post post = postService.getPostById(postId);
            byte[] document = post.getDocument();

            if (document == null) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(document);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf"); // Adjust the content type if needed

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(document.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/images/{eventId}")
    public ResponseEntity<ByteArrayResource> serveEventImage(@PathVariable("eventId") Long eventId) {
        Event event = eventService.getEventById(eventId);
        if (event != null && event.getImage() != null) {
            ByteArrayResource resource = new ByteArrayResource(event.getImage());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"event_image_" + eventId + "\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private static final String DEFAULT_IMAGE_PATH = "/static/img/user.jpg";
    @GetMapping("/notificationImage/{notificationId}")
    public ResponseEntity<ByteArrayResource> serveNotificationImage(@PathVariable("notificationId") Long notificationId) {
        try {
            Notification notification = notificationService.findById(notificationId);
            byte[] profileImage = notification.getProfileImage();

            if (profileImage == null) {
                // Load default image
                profileImage = Files.readAllBytes(Paths.get(DEFAULT_IMAGE_PATH));
            }

            ByteArrayResource resource = new ByteArrayResource(profileImage);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg"); // Adjust the content type if needed

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(profileImage.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/articleImage/{articleId}")
    public ResponseEntity<ByteArrayResource> serveArticleImage(@PathVariable("articleId") Long articleId) {
        try {
            Article article = articleService.getArticleById(articleId);
            byte[] image = article.getImageUrl();

            if (image == null) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(image);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg"); // Adjust the content type if needed

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(image.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/resume/{applicationId}")
    public ResponseEntity<ByteArrayResource> serveResume(@PathVariable("applicationId") Long applicationId) {
        try {
            JobApplication jobApplication = jobApplicationService.getJobApplicationById(applicationId);
            byte[] resumeContent = jobApplication.getResume(); // Assuming you store resume as byte[]

            if (resumeContent == null) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(resumeContent);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf"); // Adjust content type if necessary
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume-" + applicationId + ".pdf\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resumeContent.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
