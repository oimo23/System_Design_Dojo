package com.example.interview.repository;

import com.example.interview.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    List<Question> findByActiveTrue();
    
    List<Question> findByCategory(String category);
    
    List<Question> findByDifficulty(Question.DifficultyLevel difficulty);
    
    List<Question> findByCategoryAndActiveTrue(String category);
    
    List<Question> findByDifficultyAndActiveTrue(Question.DifficultyLevel difficulty);
    
    @Query("SELECT DISTINCT q.category FROM Question q WHERE q.active = true")
    List<String> findAllActiveCategories();
    
    @Query("SELECT q FROM Question q WHERE q.active = true ORDER BY FUNCTION('RAND')")
    List<Question> findRandomActiveQuestions();
    
    @Query("SELECT q FROM Question q WHERE q.category = :category AND q.difficulty = :difficulty AND q.active = true ORDER BY FUNCTION('RAND')")
    List<Question> findRandomQuestionsByCategoryAndDifficulty(@Param("category") String category, @Param("difficulty") Question.DifficultyLevel difficulty);
}