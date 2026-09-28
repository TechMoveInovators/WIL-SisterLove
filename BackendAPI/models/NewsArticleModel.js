let newsArticles = [];

const NewsArticleModel = {

    // Get all news articles
    getAll: () => {
        return newsArticles;
    },

    // Get news article by ID
    getById: (id) => {
        return newsArticles.find(
            article => article.id === Number(id)
        );
    },

    // Create news article
    create: (articleData) => {

        const newArticle = {
            id: newsArticles.length + 1,
            title: articleData.title,
            content: articleData.content,
            category: articleData.category,
            media: articleData.media || [],
            status: "Published",
            createdAt: new Date()
        };

        newsArticles.push(newArticle);

        return newArticle;
    }

};

module.exports = NewsArticleModel;