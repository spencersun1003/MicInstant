<<<<<<< HEAD
# MicInstant
Virtual Meeting Optimization Mobile APP Based on Intelligent Context Aware. A final project for HCIT course in Tsinghua University.

## Introduction

Virtual meeting has been a common scenario. However, existing meeting App has some sore points affecting user experience.
In this work, we collected 100 questionnaires to verify our hypothesis. We proposed and implemented our solution and further performed
user study to verify the validity of our scheme.


## Prerequisites

* Python 3.8 (Optional)
* Android Studio 4.0.1 or later

## Background

We select Tencent Meeting, the most common virtural meeting software in China, as our research traget. We collected 100 questionnaires.
 80% of the subjects use this platform at least 2 times a week. However, the possibility of having problems in the meeting is nearly 20%. 

![](/ReadmeImg/Survey1.svg)

We listed more than a dozen possible scenarios and found that there is a high concentration of problems that occur most frequently in three scenarios.
+ Multiple microphones turned on at the same time causing echo and whistling
+ Need to turn off the microphone when not speaking because of loud ambient noise
+ Forget to turn off the microphone after speaking
+ Speaking with other people at the same time

![](/ReadmeImg/Survey.svg)

We summarize the main scenarios to optimize as shown in the figure.

![](/ReadmeImg/Scenario.svg)

## Solution and Implementation

### 1. Solution

Here we propose a plugin to optimize Tencent Meeting by controlling the microphone based on
intelligent context aware. To be specific, in a online and in person combined classroom, students who attend in person could invert their phone to start talking, and the plugin
will make sure there would be only one person talking at a time. On the contrary, for students attend online, the plugin automatically controls the microphone
on and off according to hand/head gesture, speech detection and talker recognition.

![](/ReadmeImg/Solution.svg)

### 2. Program Structure

To install PyTorch 1.9.0, torchaudio 0.9.0 and the Hugging Face transformers, you can do something like this:

```
conda create -n wav2vec2 python=3.8.5
conda activate wav2vec2
pip install torch torchaudio
pip install transformers
```

Now with PyTorch 1.9.0 and torchaudio 0.9.0 installed, run the following commands on a Terminal:

```
python create_wav2vec2.py
```
This will create the PyTorch mobile interpreter model file `wav2vec2.ptl`. Copy it to the Android app:
```

mkdir -p app/src/main/assets
cp wav2vec2.pt app/src/main/assets
```

### 2. Build and run with Android Studio

Start Android Studio, open the project located in `android-demo-app/SpeechRecognition`, build and run the app on an Android device. After the app runs, tap the Start button and start saying something; after 6 seconds (you can change `private final static int AUDIO_LEN_IN_SECOND = 6;` in `MainActivity.java` for a shorter or longer recording length), the model will infer to recognize your speech. Some example recognition results are:

![](screenshot1.png)
![](screenshot2.png)
![](screenshot3.png)
>>>>>>> 1ab8eb7 (Main)
