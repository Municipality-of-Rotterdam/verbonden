/**
 * Pasfoto crop functionality.
 * Uses Cropper.js 2.x (loaded via WebJar) to allow
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

            // Initialize Cropper.js 2.x with passport photo aspect ratio (35:45)
            var cropper = new Cropper.default(imgElement, {
                template: '<cropper-canvas background>'
                    + '<cropper-image rotatable="false" scalable="false" skewable="false" translatable></cropper-image>'
                    + '<cropper-shade hidden></cropper-shade>'
                    + '<cropper-handle action="select" plain></cropper-handle>'
                    + '<cropper-selection initial-coverage="0.8" movable resizable aspect-ratio="' + (35 / 45) + '">'
                    + '<cropper-grid role="grid" bordered covered></cropper-grid>'
                    + '<cropper-crosshair centered></cropper-crosshair>'
                    + '<cropper-handle action="move" theme-color="rgba(255, 255, 255, 0.35)"></cropper-handle>'
                    + '<cropper-handle action="n-resize"></cropper-handle>'
                    + '<cropper-handle action="e-resize"></cropper-handle>'
                    + '<cropper-handle action="s-resize"></cropper-handle>'
                    + '<cropper-handle action="w-resize"></cropper-handle>'
                    + '<cropper-handle action="ne-resize"></cropper-handle>'
                    + '<cropper-handle action="nw-resize"></cropper-handle>'
                    + '<cropper-handle action="se-resize"></cropper-handle>'
                    + '<cropper-handle action="sw-resize"></cropper-handle>'
                    + '</cropper-selection>'
                    + '</cropper-canvas>'
            });
            imgElement._cropper = cropper;
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

        // On form submit: set crop coordinates from Cropper.js selection
        form.addEventListener('submit', function () {
            if (imgElement._cropper) {
                var selection = imgElement._cropper.getCropperSelection();
                if (selection) {
                    cropXInput.value = Math.round(selection.x);
                    cropYInput.value = Math.round(selection.y);
                    cropWidthInput.value = Math.round(selection.width);
                    cropHeightInput.value = Math.round(selection.height);
                }
            }
        });
    }
})();
