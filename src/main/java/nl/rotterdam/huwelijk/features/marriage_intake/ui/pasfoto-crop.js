/**
 * Pasfoto crop functionality.
 * Uses Cropper.js (loaded via CDN link in the page header) to allow
 * the user to select a crop area on the uploaded photo.
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        initPasfotoCrop();
    });

    function initPasfotoCrop() {
        var fileInputs = document.querySelectorAll('.rd-pasfoto-file-input');
        fileInputs.forEach(function (fileInput) {
            fileInput.addEventListener('change', function () {
                handleFileSelected(fileInput);
            });
        });
    }

    function handleFileSelected(fileInput) {
        var files = fileInput.files;
        if (!files || files.length === 0) {
            return;
        }

        var file = files[0];
        if (!file.type.startsWith('image/')) {
            return;
        }

        var form = fileInput.closest('form');
        if (!form) {
            return;
        }

        var overlay = form.querySelector('.rd-pasfoto-crop-overlay');
        var imgElement = form.querySelector('.rd-pasfoto-crop-dialog__image');
        var cancelBtn = form.querySelector('.rd-pasfoto-crop-dialog__cancel-btn');
        var cropXInput = form.querySelector('.rd-crop-x');
        var cropYInput = form.querySelector('.rd-crop-y');
        var cropWidthInput = form.querySelector('.rd-crop-width');
        var cropHeightInput = form.querySelector('.rd-crop-height');

        if (!overlay || !imgElement) {
            return;
        }

        // Read file and show in crop dialog
        var reader = new FileReader();
        reader.onload = function (e) {
            imgElement.src = e.target.result;
            overlay.classList.remove('d-none');

            // Destroy any existing cropper instance
            if (imgElement._cropper) {
                imgElement._cropper.destroy();
            }

            // Initialize Cropper.js with passport photo aspect ratio (35:45)
            imgElement._cropper = new Cropper(imgElement, {
                aspectRatio: 35 / 45,
                viewMode: 1,
                autoCropArea: 0.8,
                movable: false,
                rotatable: false,
                scalable: false,
                zoomable: true,
                guides: true,
                center: true,
                background: true
            });
        };
        reader.readAsDataURL(file);

        // Cancel button: hide overlay and reset
        cancelBtn.addEventListener('click', function () {
            if (imgElement._cropper) {
                imgElement._cropper.destroy();
                imgElement._cropper = null;
            }
            overlay.classList.add('d-none');
            fileInput.value = '';
        });

        // On form submit: set crop coordinates from Cropper.js data
        form.addEventListener('submit', function () {
            if (imgElement._cropper) {
                var data = imgElement._cropper.getData(true); // rounded integer values
                cropXInput.value = data.x;
                cropYInput.value = data.y;
                cropWidthInput.value = data.width;
                cropHeightInput.value = data.height;
            }
        });
    }
})();
