const MediaModel = require("../models/MediaModel");

const createMedia = (req, res) => {
    const {
        title,
        type,
        url,
        description
    } = req.body;

    if (!title || !type || !url) {
        return res.status(400).json({
            message: "Title, type and URL are required"
        });
    }

    if (type !== "image" && type !== "video") {
        return res.status(400).json({
            message: "Type must be image or video"
        });
    }

    const media = MediaModel.create({
        title,
        type,
        url,
        description
    });

    res.status(201).json({
        message: "Media created successfully",
        media: media
    });
};

const getMedia = (req, res) => {
    const mediaFiles = MediaModel.getAll();

    res.status(200).json({
        message: "Media retrieved successfully",
        media: mediaFiles
    });
};

const getMediaById = (req, res) => {
    const media = MediaModel.getById(req.params.id);

    if (!media) {
        return res.status(404).json({
            message: "Media not found"
        });
    }

    res.status(200).json({
        media: media
    });
};

module.exports = {
    createMedia,
    getMedia,
    getMediaById
};
