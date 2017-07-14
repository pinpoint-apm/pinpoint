(function() {
	"use strict";
	/**
	 * (en)scatterDirective
	 * @ko scatterDirective
	 * @group Directive
	 * @name scatterDirective
	 * @class
	 */
	pinpointApp.constant("scatterDirectiveConfig", {
		"scatterDataUrl": "getScatterData.pinpoint",
		template: "<div id='scatter' style='padding-top:10px;padding-bottom:20px;user-select:none;'></div>",
		images: {
			"config": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QTI2NzMzRDI2QTlGMTFFM0E1RENBRjZGODkwRDBCMEIiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QTI2NzMzRDM2QTlGMTFFM0E1RENBRjZGODkwRDBCMEIiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpBMjY3MzNEMDZBOUYxMUUzQTVEQ0FGNkY4OTBEMEIwQiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpBMjY3MzNEMTZBOUYxMUUzQTVEQ0FGNkY4OTBEMEIwQiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pn/ejkcAAAFwSURBVHjanFKxSsNQFE2CFEtaTNsMCbSQ4lA6GSghi2igo/o/GVz6AfmD/EFGJ7cKDiVLO0hFEByzpG1EA5LUxntC80jFKhi4LzfnnnPfzXmPz7KM+9ezT6iq6gnF+T4Nny88/110VK1WbwRB4OM4vgyC4PVXIQlW9JqRwGo2mzm2XC65zWYzplSnBo1CeFDu1Ov1XhaLhVWpVBimKAqXJInVarWmJGQ42xHjybIcQSSK4rNt29cgOI5jR1Gkk5gLw1DC2LkvWGBCu90eDwaDDOF53hXhhwjf908LHByYBo2ArqZpfnY6HbEYw3XdJ5riAzEajR4LHJxut6syhyhq5c6apt1hdER5EnCIKzFzqPM7fUwkSZrhf+r1+lmaphFqjUZuJIeaYRgT4q7ZqFvxmn7+GAQYBDcRyIGhBs6PN4dyjKLP5/OL4XA4RSAHhlpZs3OO1PF+W3ggwxQ6u7d+v3+7s9Nfd3VrQm3fXf0SYADyptv3yy4A0QAAAABJRU5ErkJggg==",
			"download": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RUU3MjQyMUQ2QTlGMTFFM0IxRTY4MjI3MUU5MUUyMzMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RUU3MjQyMUU2QTlGMTFFM0IxRTY4MjI3MUU5MUUyMzMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpFRTcyNDIxQjZBOUYxMUUzQjFFNjgyMjcxRTkxRTIzMyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpFRTcyNDIxQzZBOUYxMUUzQjFFNjgyMjcxRTkxRTIzMyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PlnySxAAAADPSURBVHjaxFLBDYMwDLQjMgEf4M8E4Y0YgzHaTTpGFuDfARAdoH3DiwmQSLmqRIZSpPLpSTH4krOjc9g5R0cQIDDzgozj2Ffrum6xOTcKtqolSUJCuNlR0UH8WTiZcpPGzEaB3xVWVXVO0/QhOeTgP1rKOU7/QdM0RZ7nd2OMwxc5eHkeixEm+71aKUVhGJLWmoZhoL7vaRxHX7xtW/ZzlHOTgDiKIirL8oTcWnuZ914dsyzbfXfoCuAmdV3z15ezBgRr8Nuc4ocRXhGeAgwAFHJVgfQ6KdUAAAAASUVORK5CYII=",
			"fullscreen": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QkMwOTc3QTg2QTlGMTFFMzk5MzhBOTM4OEFCMzg3MTciIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QkMwOTc3QTk2QTlGMTFFMzk5MzhBOTM4OEFCMzg3MTciPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpCQzA5NzdBNjZBOUYxMUUzOTkzOEE5Mzg4QUIzODcxNyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpCQzA5NzdBNzZBOUYxMUUzOTkzOEE5Mzg4QUIzODcxNyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PvZjiPIAAAE/SURBVHjanFKxaoRAEHXhbM4UopViGWzv0C5VUvsJqdP7A/6Dfb7DMmCVSuRCunClaKUYSKUQM2+zY1SsMjDuuDtv9u3ME9M0af+xAz6u687opmnENslxnFX1uq6FhhvDMKRluofvVeczuMr9vREmhMhRmRy/F3IukhOjM7Mh4B9VNkqQq67rR9u25VnbtsdxHPkZ6zcWRfFAN2qGYVzx3/e9X1XVC2LLsnzTNK8MQK5kCL4AwcqylPTiOH4m8C1igNI0fUIcBEGu3rwGRlFkK3qvRM9XtD+I9h3iLMvaXaDneRdF78T0cHPXdW+Iif6ZgavmALClB9q0nBRw3RyMAa0mWnJzGIbvJEneOeb9hRgEK0e2mjtG9kX+qeJH8hs163lkh2UlVWAruYIlp8ShzeNYqEQqaE9ym638R4ABADZiqF446UJLAAAAAElFTkSuQmCC",
			"error": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAABmJLR0QA/wD/AP+gvaeTAAADvUlEQVRoge2ZQW8bRRTHf2+2tMTrkCYFkQ9QqBBKC03aOOkldqtKUcUFqeUbkAN3pFSIGxQOFRx6IN8AJeIYtVIc1xdqHAdCE3EgKeeqNAlV8dIm8u7j0FasHW+8u7E3Rcrv9mbe7P7/mtHsmx044IA9Ie14iI4fP1LdPjYKZEEHBE4g9KOkAUVwUO4rrIIuI9bt9CvrJbl5b2uv796TASc7POgJEyJ6BeiJOPyRotNGrSm78NMvcTXEMuDkMqfVeNdQLsZ9cQO3MDqZnqv8GnVgJAM6MtLlpLyvQD8BrKgva0EN0Rt2zZ6UYvFp2EGhDTzODp0wRmZABuLpC81d18jlnrnyWpjkUAac82fOqMoswht70xaaTU/1g9cKlTutElsa+CebGVbj5RXS7dEWDoGqqHc+VVhcaJEXzLNlY34EjrVVXXjWXSOjuy0nE9ShY2OvGmO+Z//EA7xued4POjLSFZQQaMCxnnwNvNcRWZGQAafL/SKwt1mjk8ucVvEWaP9WGRcXo0PNvhNNZ0CNd42XRzyAhUrTWdgxA052eFCNLnZeU2RUxBu084tL/sYdM+AJE8lpioR4mI93NPoDHT9+xNnue0D0wiwZhL/sXqdfZn7bftFUNwPVrb5zvKziAZTe6kYq42+qX0IiY0nqiYWRXF1Y1yneqUTFxEE56Q8P+QNReSvsc+x8uV2SAHAuDIfKE3jbHzfuQm+2S1AHqdPYaCDRijMm3f4gsBb6v9BooLovKqKgPPaHjQYeJCglHsKf/rBuF1JYFXgnzHPC7hrtRlR+98cNM6B3kxQTD132Rw0GTDFBJbFwhYI/rjOQPrxxB3iUqKJobHb3OXVf0DoDcvPelqrMJKspPCpM+ytRaPIdMMh3yUmKhIroVGPjDgPPf7TeSkRSNGZDn4kR61Og1mlFEajh8VmzjqYG0vnSCqI3OqspAqLfpm8vNN3iA2shu2ZPAktB/QmybDuHPg/qDDQgxeJT18hHwHpHZIVBeVhT90MplZ4EpexajfbMldeMepdkH4o8gaqIXjpa+PmP3fJaltPP/g5rDuVh++S1ZFPgoj1fqbRKDHUesOcrFdeSc0AStdJSTd2zqfmFUpjk0Aeanrnymu2mMijf0JkttgZy3XZTo62WjZ9Yl3zV7NlTWHyJMh73GT4UmEWsq+l8aSXq4L1ds14Yet9TMyHCFZTeiMM3VZgW0ak4t5MvaM9F9+V3D1c3UhmM5FBOPv/10c9/B/C/Ue4LsuoZXVFlvrvPKTcWZgccsA/8C1bvKjPyfF8QAAAAAElFTkSuQmCC"
		},
		options: {
			"containerId": "",
			"containerClass": "bigscatterchart",
			"width": 400,
			"height": 250,
			"minX": 0, maxX: 1000,
			"minY": 0, maxY: 10000,
			"minZ": 0, maxZ: 5,
			"bubbleSize": 3,
			"labelX": "",
			"labelY": "(ms)",
			"realtime": false,
			"chartAnimationTime": 300,
			"gridAxisStyle": {
				"lineWidth": 1,
				"lineDash": [1, 0],
				"globalAlpha": 1,
				"strokeStyle" : "#e3e3e3"
			},
			"padding" : {
				"top": 40,
				"left": 50,
				"right": 40,
				"bottom": 30
			},
			"typeInfo": {
				/*[ typeName, typeColor, typeDisplayOrder ]*/
				"0": [ "Failed", "#d62728", 20],
				"1": ["Success", "#2ca02c", 10]
			},
			"propertyIndex": {
				"x": 0,
				"y": 1,
				"meta": 2,
				"transactionId": 3,
				"type": 4,
				"groupCount": 5
			},
			"checkBoxImage": {
				"checked" : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6ODk0MjRENUI2Qjk2MTFFM0E3NkNCRkIyQTkxMjZFQjMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6ODk0MjRENUM2Qjk2MTFFM0E3NkNCRkIyQTkxMjZFQjMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo4OTQyNEQ1OTZCOTYxMUUzQTc2Q0JGQjJBOTEyNkVCMyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo4OTQyNEQ1QTZCOTYxMUUzQTc2Q0JGQjJBOTEyNkVCMyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PkJ02akAAAEfSURBVHjalJI/aoRQEMbnrU8RRVYsBAXJASxEtLGRXEDIJXKTpPcOHiDl1gsp1QvEIiD2AbGLmpm3f7Is68YMjOOb9/0Y/Rg2zzPEcTzDP6IsS7Y5QXTAsibFIH4BQVVVi1Mcx9liyTFN13W/+JpPI0iSpL1hGEHf9+E0TSBAxtifkGVZgSzLgBkMwwCbNZNOEIVpmo3v+78gigLMt+O/3IR0XW/yPH/uuu4AEqQoComeSIznhyUoDMP3cRwPoKZpLyjaqqoKJOacfy5B6Mc39QRYFMUrOtbQO4lt24Z70BlMkqSkSxJdmrMEiYiiSGwOrh6v6/oxTdMP6lGlM/Wv3RYMPY55hrMs292DKNn1kqOb4HketG0L5N5CsB8BBgCZjoUNsxfiYwAAAABJRU5ErkJggg==",
				"unchecked" : "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OEQxQ0ZERUQ2Qjk2MTFFMzg5MjNGMjAzRjdCQ0FEMjkiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6OEQxQ0ZERUU2Qjk2MTFFMzg5MjNGMjAzRjdCQ0FEMjkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo4RDFDRkRFQjZCOTYxMUUzODkyM0YyMDNGN0JDQUQyOSIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo4RDFDRkRFQzZCOTYxMUUzODkyM0YyMDNGN0JDQUQyOSIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pq1+Js8AAABTSURBVHjaYvz//z+DiYnJfwYSwJkzZxgZjY2NYZoYidQHVs9Eoia4WhY0JxDUBfQWA7KNRGlCVsfEQCYY1UhLjf9hEUtEAgAnOUZyEjlIH0CAAQDn/BlKI9rDJAAAAABJRU5ErkJggg=="
			}
		}
	});

	pinpointApp.directive("scatterDirective", ["scatterDirectiveConfig", "$rootScope", "$compile", "$timeout", "webStorage", "$window", "$http", "UrlVoService", "CommonAjaxService", "TooltipService", "AnalyticsService", "PreferenceService",
		function ( cfg, $rootScope, $compile, $timeout, webStorage, $window, $http, UrlVoService, commonAjaxService, tooltipService, analyticsService, preferenceService ) {
			return {
				template: cfg.template,
				restrict: "EA",
				replace: true,
				scope: {
					namespace: "@"
				},
				link: function (scope, element, attrs) {
					var oNavbarVoService = null, htScatterSet = {}, htLastNode = null;
					var enableRealtime = attrs["enableRealtime"] == "true" ? true : false;

					function makeScatter(target, application, w, h, scatterData) {
						// var from = oNavbarVoService.getQueryStartTime();
						// var to = oNavbarVoService.getQueryEndTime();
						// var filter = oNavbarVoService.getFilter();
						var from = UrlVoService.getQueryStartTime();
						var to = UrlVoService.getQueryEndTime();
						var filter = UrlVoService.getFilter();
						var applicationName = application.split("^")[0];
						var options = {};
						angular.copy(cfg.options, options);
						options.sPrefix = "BigScatterChart2-" + parseInt( Math.random() * 100000 );
						options.containerId = target;
						options.width = w ? w : options.width;
						options.height = h ? h : options.height;
						options.minX = from;
						options.maxX = to;
						options.errorImage = cfg.images.error;
						// options.realtime = isRealtime();
						options.realtime = UrlVoService.isRealtime() && enableRealtime;

						var oScatterChart = new BigScatterChart2(options, getAgentList(scatterData), [
							new BigScatterChart2.SettingPlugin( cfg.images.config ).addCallback( function( oChart, oValue ) {
								webStorage.add( "scatter-y-min" + scope.namespace, oValue.min );
								webStorage.add( "scatter-y-max" + scope.namespace, oValue.max );
								oChart.changeRangeOfY( oValue );
								oChart.redraw();
							}),
							new BigScatterChart2.DownloadPlugin( cfg.images.download, "PNG" ).addCallback( function( oChart ) {
								analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_DOWNLOAD_SCATTER );
							}),
							new BigScatterChart2.WideOpenPlugin( cfg.images.fullscreen ).addCallback( function() {
								// var partialURL = oNavbarVoService.isRealtime() ? "realtime/" + oNavbarVoService.getQueryEndDateTime() : oNavbarVoService.getPartialURL( false, true );
								var partialURL = UrlVoService.isRealtime() && enableRealtime ? "realtime/" + UrlVoService.getQueryEndDateTime() : UrlVoService.getPartialURL( false, true );
								$window.open( "#/scatterFullScreenMode/" + htLastNode.applicationName + "@" + htLastNode.serviceType + "/" + partialURL + "/" + getAgentList().join(","), "width=900, height=700, resizable=yes");
							}),
							new BigScatterChart2.HelpPlugin( tooltipService )
						], {
							sendAnalytics: function( type, bChecked ) {
								analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST[type === "Success" ? "TG_SCATTER_SUCCESS" : "TG_SCATTER_FAILED"], analyticsService.CONST[bChecked ? "ON" : "OFF"] );
							},
							loadFromStorage: function( key ) {
								return webStorage.get( key + scope.namespace );
							},
							onSelect: function( oDragAreaPosition, oDragXY ) {
								if ( arguments.length === 5 ) {
									// $window.open("#/transactionList/" + oNavbarVoService.getPartialURL(true, false), application + "|" + arguments[0] + "|" + arguments[1] + "|" + arguments[2] + "|" + arguments[3] );
									$window.open("#/transactionList/" + UrlVoService.getPartialURL(true, false), application + "|" + arguments[0] + "|" + arguments[1] + "|" + arguments[2] + "|" + arguments[3] + "|" + arguments[4]  );
								} else {
									var token = application + "|" + oDragXY.fromX + "|" + oDragXY.toX + "|" + oDragXY.fromY + "|" + oDragXY.toY + "|" + arguments[2] + "|" + arguments[3];
									// $window.open("#/transactionList/" + oNavbarVoService.getPartialURL(true, false), token);
									$window.open("#/transactionList/" + UrlVoService.getPartialURL(true, false), token);
								}
							},
							onError: function() {

							}
						}, preferenceService.getAgentAllStr() );

						$timeout(function () {
							if (angular.isUndefined(scatterData)) {
								oScatterChart.drawWithDataSource( new BigScatterChart2.DataLoadManager( applicationName, filter, {
									"url": cfg.scatterDataUrl,
									"realtime": UrlVoService.isRealtime() && enableRealtime,
									"realtimeInterval": 2000,
									"realtimeDefaultTimeGap": 3000,
									"realtimeResetTimeGap": 20000,
									"fetchLimit": 5000,
									"fetchingInterval": 2000,
									"useIntervalForFetching": false
								}, function( oChartXRange, nextFrom, nextTo ) {
									// oNavbarVoService.setQueryEndDateTime( nextFrom );
									UrlVoService.setQueryEndDateTime( nextFrom );
									$rootScope.$broadcast( "responseTimeSummaryChartDirective.loadRealtime", applicationName, oScatterChart.getCurrentAgent(), oChartXRange.min, oChartXRange.max );
								}));
							} else {
								oScatterChart.addBubbleAndMoveAndDraw( oScatterChart.createDataBlock( scatterData ) );
							}
							if ( attrs["namespace"] === "forMain" || attrs["namespace"] === "forFilteredMap" ) {
								$window.htoScatter[application] = oScatterChart;
							}
						}, 100);

						return oScatterChart;
					}
					function showScatter (application, w, h) {
						element.children().hide();
						pauseScatterAll();
						if ( angular.isDefined(htScatterSet[application]) ) {
							htScatterSet[application].target.show();
							if ( UrlVoService.isRealtime() && enableRealtime ) {
								commonAjaxService.getServerTime( function( serverTime ) {
									// serverTime -= 3000;
									htScatterSet[application].scatter.resume( serverTime - preferenceService.getRealtimeScatterXRange(), serverTime );
									htScatterSet[application].scatter.selectAgent( preferenceService.getAgentAllStr(), true);
								});
							} else {
								htScatterSet[application].scatter.resume();
								htScatterSet[application].scatter.selectAgent( preferenceService.getAgentAllStr(), true);
							}
						} else {
							makeNewScatter( application, w, h );
						}
					}
					function showScatterWithData(application, w, h, data) {
						if (angular.isDefined(htScatterSet[application])) {
							htScatterSet[application].scatter.addBubbleAndMoveAndDraw( htScatterSet[application].scatter.createDataBlock( data ) );
						} else {
							makeNewScatter(application, w, h, data);
							htScatterSet[application].target.hide();
						}
					}
					function makeNewScatter( application, w, h, data ) {
						var target = angular.element( cfg.template );
						var oScatter = makeScatter(target, application, w, h, data);
						htScatterSet[application] = {
							target : target,
							scatter : oScatter
						};
						element.append( target );
					}

					function showScatterBy(application) {
						element.children().hide();
						if (angular.isDefined(htScatterSet[application])) {
							htScatterSet[application].target.show();
							htScatterSet[application].scatter.selectAgent( preferenceService.getAgentAllStr(), true);
						}
					}


					function pauseScatterAll() {
						angular.forEach(htScatterSet, function (scatterSet, key) {
							scatterSet.scatter.pause();
						});
					}
					function initScatterHash() {
						for (var p in htScatterSet) {
							htScatterSet[p].scatter.abort();
						}
						htScatterSet = {};
					}
					function getAgentList( scatterData ) {
						var oDupCheck = {};
						var aAgentList = [], server;
						if ( typeof scatterData !== "undefined" ) {
							$.each( scatterData.scatter.metadata, function( key, oInfo ) {
								if ( typeof oDupCheck[ oInfo[0] ] === "undefined" ) {
									oDupCheck[ oInfo[0] ] = true;
									aAgentList.push( oInfo[0] );
								}
							});
							return aAgentList;
						}
						if ( htLastNode.agentList ) {
							return htLastNode.agentList;
						}

						if ( htLastNode.serverList ) {
							for ( server in htLastNode.serverList ) {
								var oInstanceList = htLastNode.serverList[server].instanceList;
								for (var agentName in oInstanceList) {
									aAgentList.push(oInstanceList[agentName].name);
								}
							}
						}
						return aAgentList;
					}
					// function isRealtime() {
					// 	return oNavbarVoService.getPeriodType() === "realtime";
					// }

					scope.$on("scatterDirective.initialize." + scope.namespace, function (event, navbarVoService) {
						// oNavbarVoService = navbarVoService;
						initScatterHash();
						element.empty();
					});
					scope.$on("scatterDirective.initializeWithNode." + scope.namespace, function (event, node, w, h) {
						scope.currentAgent = preferenceService.getAgentAllStr();
						htLastNode = node;
						showScatter(node.key, w, h);
					});
					scope.$on("scatterDirective.initializeWithData." + scope.namespace, function (event, application, data) {
						scope.currentAgent = preferenceService.getAgentAllStr();
						var aSplit = application.split("^");
						htLastNode = {
							applicationName: aSplit[0],
							serviceType: aSplit[1],
							key: application
						};
						showScatterWithData(application, null, null, data);
					});
					scope.$on("scatterDirective.showByNode." + scope.namespace, function (event, node) {
						htLastNode = node;
						showScatterBy(node.key);
					});
					scope.$on("responseTimeSummaryChartDirective.showErrorTransactionList." + scope.namespace, function( event, category ) {
						$window.htoScatter[htLastNode.key].selectType( "Failed" ).fireDragEvent({
							animate: function() {},
							css : function( name ) {
								return name === "left" ? cfg.options.padding.left + "px" : ( name === "top" ? cfg.options.padding.top + "px" : "0px" );
							},
							width: function() {
								return cfg.options.width - cfg.options.padding.left - cfg.options.padding.right;
							},
							height: function() {
								return cfg.options.height - cfg.options.padding.top - cfg.options.padding.bottom;
							}
						});
					});
					scope.$on("changedCurrentAgent." + scope.namespace, function( event, selectedAgentName ) {
						htScatterSet[htLastNode.key].scatter.selectAgent( selectedAgentName );
					});
				}
			};
		}
	]);
})();