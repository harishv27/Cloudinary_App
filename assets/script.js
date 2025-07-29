document.addEventListener('DOMContentLoaded', function () {
    fetchFiles();
});

let categorizedFiles = {};

function fetchFiles() {
    const gallery = document.getElementById('gallery');
    gallery.innerHTML = '<p>Loading files...</p>';

    fetch('/get-files')
        .then(response => response.json())
        .then(data => {
            categorizedFiles = data;
            gallery.innerHTML = '<p>Select a category to view files.</p>';
        })
        .catch(error => {
            console.error('Error fetching files:', error);
            gallery.innerHTML = '<p>Error loading files. Please try again later.</p>';
        });
}

function showFiles(category) {
    const gallery = document.getElementById('gallery');
    gallery.innerHTML = ''; // Clear previous content

    if (categorizedFiles[category] && categorizedFiles[category].length > 0) {
        categorizedFiles[category].forEach(fileUrl => {
            if (category === 'images') {
                const imgElement = document.createElement('img');
                imgElement.src = fileUrl;
                imgElement.alt = "Uploaded Image";
                imgElement.style.width = "200px";
                imgElement.style.margin = "10px";
                gallery.appendChild(imgElement);
            } else if (category === 'videos') {
                const videoElement = document.createElement('Video');
                videoElement.src = fileUrl;
                videoElement.controls = true;
                videoElement.style.width = "300px";
                videoElement.style.margin = "10px";
                gallery.appendChild(videoElement);
            } else if (category === 'audios') {
                const audioElement = document.createElement('audio');
                audioElement.src = fileUrl;
                audioElement.controls = true;
                audioElement.style.margin = "10px";
                gallery.appendChild(audioElement);
            } else {
                const linkElement = document.createElement('a');
                linkElement.href = fileUrl;
                linkElement.textContent = 'Download File';
                linkElement.target = '_blank';
                linkElement.style.display = "block";
                linkElement.style.margin = "10px";
                gallery.appendChild(linkElement);
            }
        });
    } else {
        gallery.innerHTML = `<p>No files available in the ${category} category.</p>`;
    }
}
