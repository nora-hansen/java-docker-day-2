package com.booleanuk.api.controller;

import com.booleanuk.api.model.Post;
import com.booleanuk.api.model.User;
import com.booleanuk.api.payload.response.ErrorResponse;
import com.booleanuk.api.payload.response.PostListResponse;
import com.booleanuk.api.payload.response.PostResponse;
import com.booleanuk.api.payload.response.Response;
import com.booleanuk.api.repository.PostRepository;
import com.booleanuk.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("posts")
public class PostController {
    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * See posts of all users
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity<Response<?>> getAllPostsByAlLUsers()  {
        PostListResponse postListResponse = new PostListResponse();
        postListResponse.set(this.postRepository.findAll());
        return ResponseEntity.ok(postListResponse);
    }

    /**
     * See posts of current user
     * @return
     */
    @GetMapping
    public ResponseEntity<Response<?>> getAllPostsByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User userPosting = this.userRepository.findByUsername(username).orElse(null);
        if(userPosting == null)
        {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("No user with that username found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        PostListResponse postListResponse = new PostListResponse();
        postListResponse.set(userPosting.getPosts());
        return ResponseEntity.ok(postListResponse);
    }

    /**
     * Create post as user
     * @param post
     * @return
     */
    @PostMapping
    public ResponseEntity<Response<?>> createAPost(@RequestBody Post post)    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User userPosting = this.userRepository.findByUsername(username).orElse(null);
        if(userPosting == null)
        {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("No user with that username found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        post.setUser(userPosting);
        this.postRepository.save(post);
        userPosting.addPost(post);
        this.userRepository.save(userPosting);

        PostResponse postResponse = new PostResponse();
        postResponse.set(post);
        return new ResponseEntity<>(postResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Response<?>> deleteAPost(@PathVariable int id)    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User userDeleting = this.userRepository.findByUsername(username).orElse(null);
        if(userDeleting == null)
        {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("No user with that username found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        Post postToDelete = this.postRepository.findById(id).orElse(null);
        if(postToDelete == null)
        {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("No post with that id found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        this.postRepository.delete(postToDelete);
        User userWhoPosted = this.userRepository.findById(postToDelete.getUser().getId()).orElse(null);
        if(userWhoPosted == null)   {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.set("The post specified was posted by a user who is null");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        userDeleting.removePost(postToDelete);
        PostResponse postResponse = new PostResponse();
        postResponse.set(postToDelete);
        return ResponseEntity.ok(postResponse);
    }

}
