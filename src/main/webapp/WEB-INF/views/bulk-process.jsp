<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bulk Process Images</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h4 class="mb-0">Bulk Process Images</h4>
                </div>
                <div class="card-body">
                    <form action="start-bulk-process" method="post">
                        <div class="form-group">
                            <label for="inputFolder">Input Folder</label>
                            <input type="text" class="form-control" id="inputFolder" name="inputFolder" required>
                        </div>
                        <div class="form-group">
                            <label for="outputFolder">Output Folder</label>
                            <input type="text" class="form-control" id="outputFolder" name="outputFolder" required>
                        </div>
                        <div class="form-group">
                            <label>Select Dimensions</label><br>
                            <input type="checkbox" id="size100" name="sizes" value="100x100">
                            <label for="size100">100x100</label><br>
                            <input type="checkbox" id="size200" name="sizes" value="200x200">
                            <label for="size200">200x200</label><br>
                            <input type="checkbox" id="size300" name="sizes" value="300x300">
                            <label for="size300">300x300</label><br>
                            <input type="checkbox" id="size400" name="sizes" value="400x400" checked>
                            <label for="size400">400x400</label><br>
                            <input type="checkbox" id="size500" name="sizes" value="500x500">
                            <label for="size500">500x500</label><br>
                            <input type="checkbox" id="size600" name="sizes" value="600x600">
                            <label for="size600">600x600</label><br>
                            <input type="checkbox" id="size700" name="sizes" value="700x700">
                            <label for="size700">700x700</label><br>
                            <input type="checkbox" id="size800" name="sizes" value="800x800">
                            <label for="size800">800x800</label><br>
                            <input type="checkbox" id="size900" name="sizes" value="900x900">
                            <label for="size900">900x900</label><br>
                            <input type="checkbox" id="size1000" name="sizes" value="1000x1000">
                            <label for="size1000">1000x1000</label><br>
                        </div>
                        <div class="form-group">
                            <label for="quality">Quality (%)</label>
                            <input type="number" class="form-control" id="quality" name="quality" min="1" max="100" required>
                        </div>
                        <div class="form-group">
                            <label>Select Image Format</label><br>
                            <input type="radio" id="formatWEBP" name="format" value="webp" required checked>
                            <label for="formatWEBP">WEBP</label>
                            <input type="radio" id="formatJPEG" name="format" value="jpeg" required>
                            <label for="formatJPEG">JPEG</label><br>
                            <input type="radio" id="formatPNG" name="format" value="png" required>
                            <label for="formatPNG">PNG</label><br>
                        </div>
                        <button type="submit" class="btn btn-primary btn-block">Process Images</button>
                    </form>
                    <hr>
                    <a href="/" class="btn btn-secondary btn-block">Single Image Process</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.5.2/dist/umd/popper.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
<script>
    $(document).ready(function () {
        var takenTime = '<%= session.getAttribute("taken-time") %>';
        if (takenTime !== 'null') {
            toastr.success('Processing completed in ' + takenTime);
        }
    });
</script>
</body>
</html>