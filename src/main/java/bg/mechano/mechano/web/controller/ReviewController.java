package bg.mechano.mechano.web.controller;

import bg.mechano.mechano.service.ReviewService;
import bg.mechano.mechano.web.dto.review.ReviewCreateRequest;
import bg.mechano.mechano.web.dto.review.ReviewResponse;
import bg.mechano.mechano.web.dto.review.ReviewThreadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.create(request);
    }

    @GetMapping("/{id}")
    public ReviewResponse getById(@PathVariable Long id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public List<ReviewResponse> list(
            @RequestParam(required = false) Long repairShopId,
            @RequestParam(required = false) Long userId
    ) {
        return reviewService.list(repairShopId, userId);
    }

    @GetMapping("/thread")
    public List<ReviewThreadResponse> listThreaded(
            @RequestParam Long repairShopId
    ) {
        return reviewService.listThreaded(repairShopId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(@PathVariable Long id) {
        reviewService.restore(id);
    }
}