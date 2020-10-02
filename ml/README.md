## TensorFlow Lite Segmentation Models
We use [DeepLab](https://github.com/tensorflow/models/tree/master/research/deeplab)-based segmentation models for this project. We use the official model checkpoints and convert them to TensorFlow Lite for making them deployable to mobile devices.

These models can be found on [TensorFlow Hub](https://tfhub.dev/s?deployment-format=lite&module-type=image-segmentation).  **Note** that some of these models are [metadata](https://www.tensorflow.org/lite/convert/metadata)-populated.

## TensorFlow Lite Style-transfer Models
These models can be found on [TensorFlow Hub](https://tfhub.dev/s?deployment-format=lite&module-type=image-style-transfer).  **Note** that some of these models are [metadata](https://www.tensorflow.org/lite/convert/metadata)-populated.

## Model Conversion Notebooks
* Segmentation model conversion notebooks are available [here](https://github.com/sayakpaul/Adventures-in-TensorFlow-Lite/blob/master/DeepLabV3).
* Style-transfer model conversion notebook is available [here](https://github.com/sayakpaul/Adventures-in-TensorFlow-Lite/blob/master/Magenta_arbitrary_style_transfer_model_conversion.ipynb).

## Model Benchmarks

**Stylization**

<div align="center"><img src="https://i.ibb.co/Kmph83z/Screen-Shot-2020-10-02-at-3-01-41-PM.png"></img></div>

<div align="center"><img src="https://i.ibb.co/FgVQr48/Screen-Shot-2020-10-02-at-3-01-56-PM.png"></img></div>

**Semantic Segmentation**

<div align="center"><img src="https://i.ibb.co/Dg8wF0z/Screen-Shot-2020-10-02-at-3-02-22-PM.png"></img></div>

<div align="center"><img src="https://i.ibb.co/Jm7VtnG/Screen-Shot-2020-10-02-at-3-02-34-PM.png"></img></div>

