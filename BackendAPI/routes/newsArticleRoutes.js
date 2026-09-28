const express = require("express");

const {
    createNewsArticle,
    getNewsArticles,
    getNewsArticleById
} = require("../controllers/NewsArticleController");

const router = express.Router();

// POST - Create news article
router.post("/", createNewsArticle);

// GET - Get all news articles
router.get("/", getNewsArticles);

// GET - Get one news article
router.get("/:id", getNewsArticleById);

module.exports = router;
