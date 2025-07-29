const express = require('express');
const cloudinary = require('cloudinary').v2;
const dotenv = require('dotenv');

// Load environment variables
dotenv.config();

const app = express();
const port = 3000;

// Cloudinary configuration
cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
    api_key: process.env.CLOUDINARY_API_KEY,
    api_secret: process.env.CLOUDINARY_API_SECRET
});

// Serve static files like HTML, CSS, and JS
app.use(express.static('assets'));

// Endpoint to fetch categorized files
app.get('/get-files', (req, res) => {
    cloudinary.api.resources({
        type: 'upload',
        max_results: 50 // Adjust as needed
    })
    .then(result => {
        const files = {
            images: [],
            videos: [],
            audios: [],
            others: []
        };

        result.resources.forEach(resource => {
            const fileType = resource.resource_type; // image, video, raw (audio files may be categorized as "raw")
            if (fileType === 'image') {
                files.images.push(resource.secure_url);
            } else if (fileType === 'video') {
                files.videos.push(resource.secure_url);
            } else if (fileType === 'raw') {
                files.audios.push(resource.secure_url);
            } else {
                files.others.push(resource.secure_url);
            }
        });

        res.json(files);
    })
    .catch(error => {
        console.error('Error fetching files from Cloudinary:', error);
        res.status(500).json({ error: 'Failed to fetch files' });
    });
});

// Start the server
app.listen(port, () => {
    console.log(`Agri Expert app running at http://localhost:${port}`);
});
