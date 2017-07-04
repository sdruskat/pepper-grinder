---
title:  Pepper Grinder User Guide
date:
- Stephan Druskat
abstract: |
	Pepper Grinder (TraCES Edition) provides a graphical user interface for [Pepper, a conversion framework for linguistic data](http://corpus-tools.org/pepper), and
	allows conversion from the custom TraCES JSON format to the ANNIS format.
geometry: margin=1in
...

# Requirements

In order to convert sucessfully between the TraCES JSON format and the ANNIS format, corpora must be split over separate directories. Each directory must contain the corpus' JSON file (`corpusEA.json`) as well as the respective annotation files (`corpusDEA.ann` and `corpusTEA.ann`).

Only one corpus can be converted at any one time with Pepper Grinder.

# Installation

Extract the ZIP archive to a destination of your choice.

# Usage

Run Pepper Grinder by double-clicking on the executable (`pepper-grinder` or `pepper-grinder.exe`, depending on the operating system you use).

Using the *Browse* button, browse for a folder containing a corpus (cf. [Requirements](#requirements) for details).
Start the conversion process by clicking on *Run conversion*.

Once the corpus has been converted (may take a few minutes), the folder "ANNIS-OUTPUT" will contain both the ANNIS source files as well as an archive of these file with the ending `.zip`. Use the ZIP file to upload the corpus to ANNIS.


# License

Pepper Grinder is licensed under the Apache License, Version 2.0.

-----------------------------------------------------------------------------------------------
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     [`http://www.apache.org/licenses/LICENSE-2.0`](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-----------------------------------------------------------------------------------------------
