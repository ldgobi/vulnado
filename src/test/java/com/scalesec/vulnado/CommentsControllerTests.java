package com.scalesec.vulnado;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommentsControllerTests {

    private MockMvc mockMvc;

    @Value("${app.secret}")
    private String secret;

    private Comment mockComment(String id, String username, String body) {
        Comment comment = mock(Comment.class);
        when(comment.getId()).thenReturn(id);
        when(comment.getUsername()).thenReturn(username);
        when(comment.getBody()).thenReturn(body);
        return comment;
    }

    private void setupMockMvc(CommentsController controller) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionHandlerAdvice())
                .build();
    }

    @Test
    public void comments_Get_ShouldReturnComments() throws Exception {
        CommentsController controller = new CommentsController();
        setupMockMvc(controller);

        Comment comment1 = mockComment("1", "user1", "body1");
        Comment comment2 = mockComment("2", "user2", "body2");

        when(Comment.fetch_all()).thenReturn(Arrays.asList(comment1, comment2));
        doNothing().when(User.class);
        User.assertAuth(secret, "valid-token");

        mockMvc.perform(get("/comments")
                .header("x-auth-token", "valid-token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].body").value("body1"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[1].body").value("body2"));
    }

    @Test
    public void createComment_Post_ShouldCreateComment() throws Exception {
        CommentsController controller = new CommentsController();
        setupMockMvc(controller);

        Comment mockComment = mockComment("1", "user1", "body1");
        when(Comment.create("user1", "body1")).thenReturn(mockComment);

        mockMvc.perform(post("/comments")
                .header("x-auth-token", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user1\",\"body\":\"body1\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.body").value("body1"));
    }

    @Test
    public void deleteComment_Delete_ShouldDeleteComment() throws Exception {
        CommentsController controller = new CommentsController();
        setupMockMvc(controller);

        when(Comment.delete("1")).thenReturn(true);

        mockMvc.perform(delete("/comments/1")
                .header("x-auth-token", "valid-token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void comments_Get_ShouldReturnUnauthorizedForInvalidToken() throws Exception {
        CommentsController controller = new CommentsController();
        setupMockMvc(controller);

        doThrow(new BadRequest("Invalid token")).when(User.class);
        User.assertAuth(secret, "invalid-token");

        mockMvc.perform(get("/comments")
                .header("x-auth-token", "invalid-token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    public void createComment_Post_ShouldReturnBadRequestForInvalidInput() throws Exception {
        CommentsController controller = new CommentsController();
        setupMockMvc(controller);

        mockMvc.perform(post("/comments")
                .header("x-auth-token", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"body\":\"\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteComment_Delete_ShouldReturnNotFoundForNonExistentComment() throws Exception {
        CommentsController controller = new CommentsController();
        setupMockMvc(controller);

        when(Comment.delete("999")).thenReturn(false);

        mockMvc.perform(delete("/comments/999")
                .header("x-auth-token", "valid-token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private static class ExceptionHandlerAdvice {
        @ExceptionHandler(BadRequest.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public void handleBadRequest() {
        }

        @ExceptionHandler(ServerError.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public void handleServerError() {
        }
    }
}
