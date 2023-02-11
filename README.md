# MicInstant
Virtual Meeting Optimization Mobile APP Based on Intelligent Context Aware. A final project for HCIT course in Tsinghua University.

## Introduction

Virtual meeting has been a common scenario. However, existing meeting App has some sore points affecting user experience.
In this work, we collected 100 questionnaires to verify our hypothesis. We proposed and implemented our solution and further performed
user study to verify the validity of our scheme.

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

It costs much to reverse the built Tencent Meeting App or reach out the developer for source code to test our solution. Instead, we implement a fake UI covering on the real UI using accessibility to
modify the origin application without touching the source code of it.
We divide the whole program into two kinds of service.
+ The UI service can be subdivided into accessibility service to manage the real TM UI, and the float window service is to manage fake UI.
+ As for data service, we have sensor thread for gesture detection, audio service for speech detection and voice print check, and microphone manage thread to manage the microphone state

![](/ReadmeImg/ProgramStructure.svg)

### 3. Key Point - UI Implementation

Here we demonstrate the principle. 

![](/ReadmeImg/UI1.svg)

For fast meeting page, real UI is leftside and fake UI is right side. Red box encircles what we want to add to the real UI. Since we don’t have space to place additional component on the real UI, we have to re-implement a full screen float window that looks like real UI but a new row is added.
The float window is circled by blue box.
When user touch in the main menu to switch the page to this one, we directly cover the fake UI on the real one. 
For figure 3, blue box is what we want to add to the real UI. Luckily, we can add the button on the top of “entrance setting column”, so all we need to do is just implement a single row. When user touch the fast meeting button in main menu, we show the fake UI and cover it exactly on the word ”Settings”.

![](/ReadmeImg/UI2.svg)

### 4. Fuction Implementation

![](/ReadmeImg/Gesture.svg)
![](/ReadmeImg/TapTap.svg)
![](/ReadmeImg/Control.svg)

## Teamwork

Yue Sun: Software Developer

Evie Mo, Linda Huang: Designer

This project is implemented based on PyTorch template.

