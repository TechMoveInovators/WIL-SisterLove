const express = require("express");

const {
    createMedia,
    getMedia,
    getMediaById
} = require("../controllers/MediaController");

const router = express.Router();

router.post("/", createMedia);
router.get("/", getMedia);
router.get("/:id", getMediaById);

module.exports = router;
