const NewsArticleModel = require("../models/NewsArticleModel");

// Create news article
const createNewsArticle = (req, res) => {

    const {
        title,
        content,
        category,
        media
    } = req.body;

    // Validate required fields
    if (!title || !content || !category) {
        return res.status(400).json({
            message: "Title, content and category are required"
        });
    }

    const article = NewsArticleModel.create({
        title,
        content,
        category,
        media
    });

    res.status(201).json({
        message: "News article created successfully",
        article: article
    });
};


// Get all news articles
const getNewsArticles = (req, res) => {

    const articles = NewsArticleModel.getAll();

    res.status(200).json({
        message: "News articles retrieved successfully",
        articles: articles
    });
};


// Get one news article
const getNewsArticleById = (req, res) => {

    const article = NewsArticleModel.getById(req.params.id);

    if (!article) {
        return res.status(404).json({
            message: "News article not found"
        });
    }

    res.status(200).json({
        article: article
    });
};


module.exports = {
    createNewsArticle,
    getNewsArticles,
    getNewsArticleById
};