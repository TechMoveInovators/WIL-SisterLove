const express = require('express');
const helmet = require('helmet'); // this will be discussed later
const dotenv = require('dotenv');

dotenv.config();

const app = express();

app.use(helmet());
app.use(express.json());

app.get('/', (req, res) => {
res.send('SisterLoverSA API running!');
});

module.exports = app;