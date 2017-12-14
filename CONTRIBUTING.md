Thank you very much for choosing to share your contribution with us. Please read this page to help us better realize your goals.

## Issues
Feel free to open issues on just about anything related to Pinpoint. These can be anything from bug report to feature proposal. A few points to consider before opening a new issue:
* Please make sure you've searched through the documentation and existing issues to see if what you're posting has already been addressed.
* If your build fails, please take a look at [Travis build status](https://travis-ci.org/naver/pinpoint) to check our current build status.
* If something is not working, please be as detailed as possible in your description. The more information you provide, the easier it will be for us to provide you with a solution.
* If something not directly related to Pinpoint is not working (such as HBase), try and see if it's something you can get fixed. Chances are, you might be much better equipped to solve the problem. Even if nothing works, being able to share everything you've tried with us will help resolve the issue a lot quicker.
* For quick questions, or issues that aren't directly related to the code base, please consider posting them to [Pinpoint Google Group](https://groups.google.com/forum/#!forum/pinpoint_user).

## Pull Requests
Apart from trivial fixes such as typo or formatting, all pull requests should have a corresponding issue associated with them. It is always helpful to know what people are working on, and different (often better) ideas may pop up while discussing them.
Please keep these in mind before you create a pull request:
* Every new java file must have a copy of the license comment. You may copy this from an existing file.
* Make sure you've tested your code thoroughly. For plugins, please try your best to include integration tests if possible.
* Before submitting your code, make sure any changes introduced by your code does not break the build, or any tests.
* Clean up your commit log into logical chunks of work to make it easier for us to figure out what and why you've changed something. (`git rebase -i` helps)
* Please try best to keep your code updated against the master branch before creating a pull request.
* Make sure you create the pull request against our master branch.
* If you've created your own plugin, please take a look at [plugin contribution guideline](../../wiki/Pinpoint-Plugin-Developer-Guide#iii-plugin-contribution-guideline).

Please make sure you've signed the [Contributor License Agreement](https://docs.google.com/forms/d/e/1FAIpQLSfNuUx0lkiapWF8LE0xQSVL-ZNheuy2LEIixyqCj9y5GsSzVQ/viewform?c=0&w=1) before contributing your code. This isn't a copyright - it gives us (Naver) permission to use and redistribute your code as part of the project.
