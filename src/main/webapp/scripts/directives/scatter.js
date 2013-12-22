'use strict';

pinpointApp.constant('scatterConfig', {
    get: {
        scatterData: '/getScatterData.pinpoint',
        lastScatterData: '/getLastScatterData.pinpoint'
    },
    useIntervalForFetching: false,
    nFetchingInterval: 2000,
    nFetchLimit: 5000,
    options: {
        sContainerId: '',
        nWidth: 400,
        nHeight: 250,
        nXMin: 0, nXMax: 1000,
        nYMin: 0, nYMax: 1000,
        nZMin: 0, nZMax: 5,
        nBubbleSize: 3,
        sXLabel: '',
        sYLabel: '(ms)',
        sTitle: '',
        htTypeAndColor: {
            // type name : color
            'Success': '#2ca02c',
            // 'Warning' : '#f5d025',
            'Failed': '#d62728'
        },
        htOption: {
            dataType: 'jsonp',
            jsonp: '_callback'
        },
        index: {
            x: 0,
            y: 1,
            transactionId: 2,
            type: 3
        },
        type: {
            '0': 'Failed',
            '1': 'Success'
        },
        'htCheckBoxImage': {
            //                'checked' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QTQ5RjA0MDI2N0IwMTFFMzk4NzBFMzU4NEZGNzJGQTIiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QTQ5RjA0MDM2N0IwMTFFMzk4NzBFMzU4NEZGNzJGQTIiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpBNDlGMDQwMDY3QjAxMUUzOTg3MEUzNTg0RkY3MkZBMiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpBNDlGMDQwMTY3QjAxMUUzOTg3MEUzNTg0RkY3MkZBMiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Ph3EYZoAAAG8SURBVHjanFK/SwJhGL6TU1DO/IWiIprQcA562uAkIriEk9DSVhC1hn9Fo4LhKDU4Ro3RUoOzCo4KtZQNQVyZIKjX8xzdobR18Hzfd+/7PO+v7xN1XRf+80lcRFG0DJFI5BDbEbANeIEBcDmZTK5MDpOJxgIhBB7YHl0uVxYQHA7HlPb5fC7PZjMBYIASAmhGlWapsVhskMlk9HK53G+328ewFwieaaOPHDOjsbA8U4T/LCCvlSXTZorJpcZGp8/nO7Xb7UKlUjlHeQNg+tuvJxqNNoEn+sgJBAIHVqOqqn7m8/kvMxMEZ0CCpaVSKT0ej1/RR04ul3uxMjqdzu/VasUhTSmSZbkhSdKz3+9Xg8HguNPpXNBHjs1m26LGEGKCr8vlUoZIbbVa99jHoVBI8Hq940ajcVIoFPr0keN2u0eWMJlM3iISMzer1eqIZPR1xx2ldZFtQR85iUTixuoRCBeLxVE6nebUHhRF2YVNASRmoo0+csjduMder1cyxRgGA3wQPJsicv68HEaHLVur1faHw+Gepmk7GNCUfaKCbr1ev+bzY9kbwrULD2MjeDULgHf6Ds7b+lv9EWAAPSv5uXpE5/kAAAAASUVORK5CYII=',
//            'checked' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MkIxMzI0NEE2N0IyMTFFMzg2NzY5QUU5RUVGNzAzMDQiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MkIxMzI0NEI2N0IyMTFFMzg2NzY5QUU5RUVGNzAzMDQiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDoyQjEzMjQ0ODY3QjIxMUUzODY3NjlBRTlFRUY3MDMwNCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDoyQjEzMjQ0OTY3QjIxMUUzODY3NjlBRTlFRUY3MDMwNCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pj7AdegAAAPaSURBVHjaXJN7UFRVHMe/995zL7t32WV5JIpCQoOQa0vqBDJMZjrYECSSEQMCwWZJhElZgJhYQDpGDy3sD3lMkJgDJiGaDWHDZEPGZBqZafIQWGTlueyDu+/bFeyffjPnnN/MmfOd8/t+fj/K43JBf6GKmbjVkT3Ix35R09AOvY21ywhF4UG4BDMjVwcqHtp+7Nz2R025aa7qyT/mNJvyh7YZTSJ/lVAwBc9ahcrqtltJF0f1Ml8fFeQEMrdHBE2JEEURioAQLHvxvU6F5rH8EGfTJOzIOdvR85nVqZx0Ld+QQyy3W1+q/7o9u9MYCu9F/IBoHrlm9nCclxcLj80MmrBkydbicaU2rjjX3mhYZz+ft++s9XDTxb+UxNmv1O5a2Uy6v2vfe67HhJWbtuG5vNSW2uL8kvVBNlByf4z6pmO4sw5E/TBeFU8wWuF84TtnTAebL92Ww+6N6M067M6K/Yb0dN102ngV/LjFSI5bQa849CaiAq4E1TUPridxaXphZuznBOuXZJVt7K3i5tnytl/7vSDI8PSWAhRU6iqX+bPlZGpU5vEiHghOBxxu2OKfjKB6Oy9XX+q6voURvjf4Bkfqlsy2rDpwiqlouzzAUdIP4tMK8fqBzNIgH+ZDE+Am8whE4P4pZ6TMairoaOxN+advEgrz8aUKXtlUBSs/Nj3LMU4lNmbscReVZ+wJHBs+2ndtAmSdVgLwACQtvTe4QayK0M937sv7KusJDUTTLKzmqYB7MxbeaVcjPr1k7u2y9NfCORz1TE/nCjOWTyV64bREcD4ozCfcnJQpI7Q7io6UNqSvjQRlm5PK9MPmjCJrUUXq7nB+pgbClf1nTtXXdg1YC1mae4QWFxTmJYi0OaHG1Zt3HA7l8py99VXHX45OFDIyS++VvZ/yRigRa2lhurzlyCflB+su0BM2Ao4GIf5LbbT9hgwcK8NiAnFkeBAFefuRsDEGUWsf34WA4M4Ar7vG0yebfohas1qjMQ690Nr0E8blgVCF+E/5MTCQ6A2RbONvf2LKbkBLd5/n2w/yMX6nHydOjuGjj+sccn+uhRJsmDBYkJn1rKMwcY1gdBL4BYVhkSakVUXhOmX+u+7dspLqitMDPBQ8P8BKHcvKeY6SHHc53aBoqU5pBFxuOJRy2tuXMcYOmRRKbfahu8k7k1K3qtFNvMNTGnTpQ+HGY7VJP44qwjiVKoyRBDyS4yxLFsxiAJYDLBYj9B6pMWN0va88/1RNjBq9zIKXqhEfhVxXkByRnfi/KRb/Q+fxYM7uEkNXJ/g8k5j1y+96doeKct/g719J618BBgDazaakCUmmjgAAAABJRU5ErkJggg==',
//            'checked' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NzE5OUE4RDM2QTlGMTFFM0EzMjdDOTdGQTE2N0M0N0MiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6NzE5OUE4RDQ2QTlGMTFFM0EzMjdDOTdGQTE2N0M0N0MiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo3MTk5QThEMTZBOUYxMUUzQTMyN0M5N0ZBMTY3QzQ3QyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo3MTk5QThEMjZBOUYxMUUzQTMyN0M5N0ZBMTY3QzQ3QyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Ph3vUz4AAAG8SURBVHjanFK/SwJhGL6TU1DO/IWiIprQcA562uAkIriEk9DSVhC1hn9Fo4LhKDU4Ro3RUoOzCo4KtZQNQVyZIKjX8xzdobR18Hzfd+/7PO+v7xN1XRf+80lcRFG0DJFI5BDbEbANeIEBcDmZTK5MDpOJxgIhBB7YHl0uVxYQHA7HlPb5fC7PZjMBYIASAmhGlWapsVhskMlk9HK53G+328ewFwieaaOPHDOjsbA8U4T/LCCvlSXTZorJpcZGp8/nO7Xb7UKlUjlHeQNg+tuvJxqNNoEn+sgJBAIHVqOqqn7m8/kvMxMEZ0CCpaVSKT0ej1/RR04ul3uxMjqdzu/VasUhTSmSZbkhSdKz3+9Xg8HguNPpXNBHjs1m26LGEGKCr8vlUoZIbbVa99jHoVBI8Hq940ajcVIoFPr0keN2u0eWMJlM3iISMzer1eqIZPR1xx2ldZFtQR85iUTixuoRCBeLxVE6nebUHhRF2YVNASRmoo0+csjduMder1cyxRgGA3wQPJsicv68HEaHLVur1faHw+Gepmk7GNCUfaKCbr1ev+bzY9kbwrULD2MjeDULgHf6Ds7b+lv9EWAAPSv5uXpE5/kAAAAASUVORK5CYII=',
            'checked': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MEJCODQ3Mzg2QUEwMTFFMzkxQzFEM0IzQzRCREQ1NzQiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MEJCODQ3Mzk2QUEwMTFFMzkxQzFEM0IzQzRCREQ1NzQiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDowQkI4NDczNjZBQTAxMUUzOTFDMUQzQjNDNEJERDU3NCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDowQkI4NDczNzZBQTAxMUUzOTFDMUQzQjNDNEJERDU3NCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PnftxhQAAAD/SURBVHjaYvz//z8DOYCJgVxArI2SkpL8QDwfRIP4jCCNjIyMBDUxMzMf5OXl1f/8+fPFv3//2hPUCNMkJCSkz8rKyvD792+GDx8+LGIixiaYJhAQEBC4s3Tp0ilMSIr0gXg9zA/YNHFzc9+ZMGFCqo2NzXmwU6WkpPTZ2NgO8vDw8H/8+BHkB3+gpo3YNBkaGh4Beu0PC0iQi4urhY+Pj5+JiYkBqEH//fv3DwQFBRlwaYLH4+LFi1uBIXYHxAYpFhMTw6sJHo9AzHLu3DkHOzu728bGxv9hGMQHiYPkcSYAdM14NaGnHJhmX1/f7Xg1YUtyIMVArIFXExAABBgAMfmWOGZ7FUYAAAAASUVORK5CYII=',
            //                'unchecked' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QjE5RDlGMzg2N0IwMTFFM0E3ODNCMUJCRkQxNDcyRUMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QjE5RDlGMzk2N0IwMTFFM0E3ODNCMUJCRkQxNDcyRUMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpCMTlEOUYzNjY3QjAxMUUzQTc4M0IxQkJGRDE0NzJFQyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpCMTlEOUYzNzY3QjAxMUUzQTc4M0IxQkJGRDE0NzJFQyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PrbSL8QAAAGmSURBVHjanFJBSwJBGN1dVkFZ2hUvuoiE7GE96GqCEMQiCBKCB+nSbYOguz+gc0f/gdUP6Fy3IqKjCl6E9lp2CGNrMwR1+p44i9athTfz5n3vm/n2mxEZY8J/PhmDKIqBkEwmHZqOCNsEjdAnXIxGo0vuwWHicqBESlBJu4tGowWCEA6HfejT6VSZTCYCARtUaANvWSUvNZVK9fP5PKtWq71Op3NM+h4ADg0xePiJywHl8SRaF2q12i4vCxwaT4YXORKCsVjsJBQKCfV6/UzXdTYcDh/JcA6AQ0MMnng8fhj8qGVZH+Vy+ZO4AjQajetsNssAcK7DUywWn4MTI5HI12KxQJPQEH88Hn+jQQA41+GRJGkruA7q4At1b4dKs7Amc9MwjBtw13WbXJ/P54qqqr2gVMdxTkulEstkMvcoadVVE1hxBTF44A26SkjYtv2Uy+XQtdvfXYWGGDzwbtxjt9ut8OR0Oo0N3gFwngTPn5dDM/630Gq1DgaDwb7neYYsy76maa5pmg/tdvsKz4+8s43EtXeYoAlQCDOCT3gjz+v6W/0RYABWjxOfoGAEuQAAAABJRU5ErkJggg=='
//            'unchecked' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QzcxQkU2OUQ2N0IyMTFFMzgwMUNENjM2QzE2NjE0MEMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QzcxQkU2OUU2N0IyMTFFMzgwMUNENjM2QzE2NjE0MEMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpDNzFCRTY5QjY3QjIxMUUzODAxQ0Q2MzZDMTY2MTQwQyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpDNzFCRTY5QzY3QjIxMUUzODAxQ0Q2MzZDMTY2MTQwQyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PrdczF4AAAMiSURBVHjafFNbSBRRGP7O7MxeZtbdWk22cmvS3S4URgSFRdBTICVRVmAXxYogqKCMsDvRxSiyiC6QRQhbPlQPkST0UBFoINjVLmba1o4l3t1t3ds4pzPjZtZDB745w3/O/53/fP/5iKaqUOrOmrpbHhV/EfOuVFU/gBIT4laeEPweVEM0rtJpc5c6Zy3f9PyFImw9s1p+P987ztjDE4Q8g5HoiUv3W1Y87lCsTocDokCsGqWjHCAcJJsFwZd1eNP0LM+1cHNNaFVOlQr4GWKmvUXTd1Ve8O+u/ZFpE8dNaOeiPU8Iz7cRkE8sfRQJlTaLNnNQFgcz+768naKYvbM57/ShXDvquYaHD/bXNoYwKzcfOyuv3HHLnsL+rq4CzmQqYMkGWM0F3b2Dhdm+aTsq9mxplYfDaH54e+q92qZTeqFc49OPyZiYhnSLG2sXebmKq9fgmz0Hof5+ECaLjp+RKDyTXTh2+iDJ8iwgEywWJMPf8bquXjVIejusmoWnSCRj6FRB5CkyTlVXw+PzIRqJIJFIIiPDidu3zsHrmUF+DnWSpIkHF9cgBjo1g8SQl2lozOzTy6aMzEyc8fsxUZYhSVbU1JxDTk42WwnpKSMJRgaHke78aaQx9HCUwTV+PI7fvIms4a/ImjQxFbWP6dhoCeDGdlIfen02hj6myeHSUqxbswuKoqSiYzbrvxpNkaRYaWohnU09XV3Yt3EjfgQCiERiKCoqQ1tbO1txpI4hv1lGSNInx7i4SmAWrHDzoIFvARwoKUGwtRU2SYLZLKCnZxDrN5Thc7CF2kU3FYZVaBYOQ7LbEIVbsHSmYB0KozfeiTsNn7X927eh9V0zHEwTyu6qwy7ZEOzow9Hyk1QJNtLueBxC2iTMzV/MGxcJf7hx6Ej5peN320VIotguhIOvBJtoJvhb8aRKE6KZ2l2mgbxASErLLa74WliyonKNGxcJVfs8zXcvnKi8fJ15R3Lp3rEwR2n/KM6xNsYjA+jXJDDvvDl/qLRqiS/Nz0oZYM/MEXRKts07Vs4oXv4fF0dizMXz8p3LUi52kOH3fOp5/RJgABimRYnpsNz9AAAAAElFTkSuQmCC'
//            'unchecked' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OTM2MTc2NjI2QTlGMTFFMzg4Mzc5QjYwNTBFOUIxOTgiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6OTM2MTc2NjM2QTlGMTFFMzg4Mzc5QjYwNTBFOUIxOTgiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo5MzYxNzY2MDZBOUYxMUUzODgzNzlCNjA1MEU5QjE5OCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo5MzYxNzY2MTZBOUYxMUUzODgzNzlCNjA1MEU5QjE5OCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PjnwZmQAAAGmSURBVHjanFJBSwJBGN1dVkFZ2hUvuoiE7GE96GqCEMQiCBKCB+nSbYOguz+gc0f/gdUP6Fy3IqKjCl6E9lp2CGNrMwR1+p44i9athTfz5n3vm/n2mxEZY8J/PhmDKIqBkEwmHZqOCNsEjdAnXIxGo0vuwWHicqBESlBJu4tGowWCEA6HfejT6VSZTCYCARtUaANvWSUvNZVK9fP5PKtWq71Op3NM+h4ADg0xePiJywHl8SRaF2q12i4vCxwaT4YXORKCsVjsJBQKCfV6/UzXdTYcDh/JcA6AQ0MMnng8fhj8qGVZH+Vy+ZO4AjQajetsNssAcK7DUywWn4MTI5HI12KxQJPQEH88Hn+jQQA41+GRJGkruA7q4At1b4dKs7Amc9MwjBtw13WbXJ/P54qqqr2gVMdxTkulEstkMvcoadVVE1hxBTF44A26SkjYtv2Uy+XQtdvfXYWGGDzwbtxjt9ut8OR0Oo0N3gFwngTPn5dDM/630Gq1DgaDwb7neYYsy76maa5pmg/tdvsKz4+8s43EtXeYoAlQCDOCT3gjz+v6W/0RYABWjxOfoGAEuQAAAABJRU5ErkJggg=='
            'unchecked': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MTVERDc5MDA2QUEwMTFFM0JDQURBMzhBQUY5NzJCOUMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MTVERDc5MDE2QUEwMTFFM0JDQURBMzhBQUY5NzJCOUMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDoxNURENzhGRTZBQTAxMUUzQkNBREEzOEFBRjk3MkI5QyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDoxNURENzhGRjZBQTAxMUUzQkNBREEzOEFBRjk3MkI5QyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pm8UK2sAAAE4SURBVHjanJK/aoRAEMb9dyBYnGij1iGcVU5IcYTgXS++Q65Ikc5HsQ+Sp5C0OQgpNWmTdAGtFK6wMmLmI66sd50Dg7Oz3292ZlDs+16YY5Iw0xQWiKIo2Lb9ROGafFeW5RF5yi3p80L+Trk963AEAem6frdYLIS6rg903iIvy/LBMIyrtm3Xg3Q/AS3L2kjSf+cQAmYxisFVVd2czZgkyYOmad+IIQLAIBjuoBlH47aq5Hl+G0XRY9M0F/wiAMVxfO953isdfycglhMEgVlV1RvNc8mD9OqnaZo3aZpWo54FjuMs2SJYe8yoEBb20XXdtiiK42TGUwjtnc4MzdlyXNf94SHMBOdhaMY20OrgqzAMn33f/8qybEdnBY4YOdxBw/QjOBTBxTUArrAy5Fb8Q+Lcn/xPgAEApXmuFYuT7BAAAAAASUVORK5CYII='
        },
        //        'sConfigImage' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MzgyNzUwNkM2N0IwMTFFMzk4N0JDNjMzREE2NTYyNjQiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MzgyNzUwNkQ2N0IwMTFFMzk4N0JDNjMzREE2NTYyNjQiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDozODI3NTA2QTY3QjAxMUUzOTg3QkM2MzNEQTY1NjI2NCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDozODI3NTA2QjY3QjAxMUUzOTg3QkM2MzNEQTY1NjI2NCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PnJ75IEAAAFwSURBVHjanFKxSsNQFE2CFEtaTNsMCbSQ4lA6GSghi2igo/o/GVz6AfmD/EFGJ7cKDiVLO0hFEByzpG1EA5LUxntC80jFKhi4LzfnnnPfzXmPz7KM+9ezT6iq6gnF+T4Nny88/110VK1WbwRB4OM4vgyC4PVXIQlW9JqRwGo2mzm2XC65zWYzplSnBo1CeFDu1Ov1XhaLhVWpVBimKAqXJInVarWmJGQ42xHjybIcQSSK4rNt29cgOI5jR1Gkk5gLw1DC2LkvWGBCu90eDwaDDOF53hXhhwjf908LHByYBo2ArqZpfnY6HbEYw3XdJ5riAzEajR4LHJxut6syhyhq5c6apt1hdER5EnCIKzFzqPM7fUwkSZrhf+r1+lmaphFqjUZuJIeaYRgT4q7ZqFvxmn7+GAQYBDcRyIGhBs6PN4dyjKLP5/OL4XA4RSAHhlpZs3OO1PF+W3ggwxQ6u7d+v3+7s9Nfd3VrQm3fXf0SYADyptv3yy4A0QAAAABJRU5ErkJggg=='
//        'sConfigImage' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAAPCAYAAAACsSQRAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MzRENzIxRDU2N0IyMTFFM0IwREJENjk5N0Q3RjA4OEIiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MzRENzIxRDY2N0IyMTFFM0IwREJENjk5N0Q3RjA4OEIiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDozNEQ3MjFEMzY3QjIxMUUzQjBEQkQ2OTk3RDdGMDg4QiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDozNEQ3MjFENDY3QjIxMUUzQjBEQkQ2OTk3RDdGMDg4QiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PtUhGiAAAAPFSURBVHjaZJNfTFNXHMe/vb23vQVLaclahGGBsYdu0gEiLVDLhllSRzRmExbicMtIEx9wi8m2l/nQIEu2sWTJNJkJCcEtBgNzRl0i2QOJylyLSFvAQCKWVqECW/9cWtrb28s9u8xHz8t5Ob/f+eb7/XxByBMQ3g/CXaUWQn6mb3CJmgkGQUi06cb5jhWLxZLvOH9jJUpIUzA4g6XBPsofWmCucoTy8wRPCAGF3aOUL1Fs3IisXRTyxKUWo+zOs7ue6blcJQWKzs1NV959tuOJimqW5AXXWmTjoiiikVL+Pw0FIWEgz9nv/DJy6asLU2+x5iObZ7rL12cnr1VN/P2PVsEoQPIErzS7Uh+0N6ysjV4ovR3hjY4z3wc/OdV2WsfAS0uZNChGKkvE8zWJ5CYYcczY/y1r5NMpSLJUvcGAOJfA1uRv2p+n/7Dy4g6y6Tzy8USNBJSlMxIoIZYBmLr77Z0nZ48dKIFIGCCXhq60GvV2R1aSpDGHvT5bXapDOic/JSJKDhzDyc722ToG9zMxAVSKi2No8By1+jSiQp4F+G3AaIG27ezlzh73CVbNut09nSfOtmkvW4zANg+weSDydFV1bnCIinMpIHxvxHv8oPlxq9ORaWm0kuqqBtI7cF14kCSHfYEgbE02BAM+kOSDw9cHeoWGqmpibWwhDmdrxnzw+OORe2EvPe+dqFtI6dQsvwWVlIGkr4K23jJq1sEXkUQ5agJRktPTmX2Weu1oiV46tZKV5eQEjS6/UTPhna+gau2uwH4tt6zTF2VZTQGoRAwp/2J3hINNomgoFArQlGwhF7Et+lPdsQSFAg2LIr0uy2n3L7vstQFaU2Syv9f5UVmrrfb34R9+st2Kr2P6z2EmlFH3nD6kVSuVyqmt52HH7bFLXcNT/zJJBY3Svc3o++LTuXnfX++bijRR2rCvAu4vB6StsFcAs+taIbC5iNSdHz8eD+/t4nP8raFfx48+j6Q0KzGgcNd7OUDzvleFD51u6VFSlHEsloeEQMvk+JWGmw9joBWy9eo94NZD8HunNBRFdU15/ZrQOoc9aplLWUns4U1cGZ9sCAhoKSymQQNE5paK6g3Msr7Y+BKx0o4EXUHRS8QyBv0ypUCU7LbG4/lM3iOsVlYrZ1+rd5ENOL/rsJGvu4680bw2u/T6o2gSxjePoveb/kmTBu8eyoXmrO7Ptw1vv9NvYuFTycG9KKD8G2h6xmQu71MxiokcXcYrK5yeJqs6LEES1damsLNC6Smjc7yCUU2Um019NI0ZaedFAf8TYABcmcQ5EJfZnAAAAABJRU5ErkJggg=='
        'sConfigImage': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QTI2NzMzRDI2QTlGMTFFM0E1RENBRjZGODkwRDBCMEIiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QTI2NzMzRDM2QTlGMTFFM0E1RENBRjZGODkwRDBCMEIiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpBMjY3MzNEMDZBOUYxMUUzQTVEQ0FGNkY4OTBEMEIwQiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpBMjY3MzNEMTZBOUYxMUUzQTVEQ0FGNkY4OTBEMEIwQiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pn/ejkcAAAFwSURBVHjanFKxSsNQFE2CFEtaTNsMCbSQ4lA6GSghi2igo/o/GVz6AfmD/EFGJ7cKDiVLO0hFEByzpG1EA5LUxntC80jFKhi4LzfnnnPfzXmPz7KM+9ezT6iq6gnF+T4Nny88/110VK1WbwRB4OM4vgyC4PVXIQlW9JqRwGo2mzm2XC65zWYzplSnBo1CeFDu1Ov1XhaLhVWpVBimKAqXJInVarWmJGQ42xHjybIcQSSK4rNt29cgOI5jR1Gkk5gLw1DC2LkvWGBCu90eDwaDDOF53hXhhwjf908LHByYBo2ArqZpfnY6HbEYw3XdJ5riAzEajR4LHJxut6syhyhq5c6apt1hdER5EnCIKzFzqPM7fUwkSZrhf+r1+lmaphFqjUZuJIeaYRgT4q7ZqFvxmn7+GAQYBDcRyIGhBs6PN4dyjKLP5/OL4XA4RSAHhlpZs3OO1PF+W3ggwxQ6u7d+v3+7s9Nfd3VrQm3fXf0SYADyptv3yy4A0QAAAABJRU5ErkJggg==',
//        'sConfigImage' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA8AAAANCAYAAAB2HjRBAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QjE2REQ1M0Y2QUEwMTFFM0ExRTVFNDQ4RkFBMjBDMDMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QjE2REQ1NDA2QUEwMTFFM0ExRTVFNDQ4RkFBMjBDMDMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpCMTZERDUzRDZBQTAxMUUzQTFFNUU0NDhGQUEyMEMwMyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpCMTZERDUzRTZBQTAxMUUzQTFFNUU0NDhGQUEyMEMwMyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PtbihfcAAAMNSURBVHjabNBbTFN3AMfx3zk9vR4KaUqg4WhwNLa1Uiy0InOGadiD8TKTmqhEE32QBx/UxWm8iy5u2WKyuC0xqEhiYkJtIkm9ELNIiIjSsRYO1NQi0mBKt6q0pKUH2p72/IW3Pez9+/L5gqQfgGQ8EIVeVYc3ou1yD4JI78x9N9p8Nput0Hajz/dOIuZBdxci3g5tryCqPBmCB2kCGuo1gGyFIcKH24MB/qZE05vmwgMn+n2xDSRPZDFf/4aB8NwJmpY28YHgzTAfaV8hg2GNGmDAiFQ2Md/aed17xj2YwuK3X7sGPUHqVWAaGm0pwi88uLcwc8impw8+f+hVlr3RYO0fP3xkK1XXGWA1Uemq4vXr6wVuzMP2Pe1Ryig5yvR6yGgS08tZbto/oJgkRRS1HDYvdQ06VZwFCDMz9hdmhUKw1mpKcholm5IUYOtcE65v7N6hx3efHNzx5Xb+mWdXz3janNdwMFlrk7yfDzJsOZhPEX/XT7/fM5FSQ0UsTaHK0oK2s9/fLy98uOp7TIm19euGWtZz2VT77UtPwzF4b12piKepjgPHzr1lJsL8Pv/7BbVaGUFWolFZ55hqtOoflTMl4l2NCsaGRlFZmH3kqOve/2coboy8DSmRW/yKD0800GaL3e2s1ry0mmpyWlpCZjxgHA4ldg6PTMmzC1lMjQzLE6HhnYHxjFGitagxWXOaaudLu8XspqJ8/7K5VjY32Xv6wrWVoYwCpUvmPf9nLrHi1NVfoqt1xW1L5tcUIQtAXmjt/u3H2z93ethUTob/3qakIpdIpCAu31aWYc/hM8L5461trALdNMgklU2OGEb/HmVj8xVo2erKNTtX5YVkAoVcgUskBaxyNuddW1tyFfMxLHcjyaxhkoCSXb5wDIyCTFdXlxSEsl3Rxqamo9/trSLp6EdH6P0cjBt3Y9/Fy3c2l2ROOlaWqw0HjjwxflHZyVLIMFh8A9DFeI3dcsU2a5HTi//M6yzNn7Y0Da3zjkadXNMWf7NF92t8nJ6wO2xjKnuNOFNE9l8R+CzAAKmNcMexeFiOAAAAAElFTkSuQmCC',
        'sDownloadImage': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RUU3MjQyMUQ2QTlGMTFFM0IxRTY4MjI3MUU5MUUyMzMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RUU3MjQyMUU2QTlGMTFFM0IxRTY4MjI3MUU5MUUyMzMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpFRTcyNDIxQjZBOUYxMUUzQjFFNjgyMjcxRTkxRTIzMyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpFRTcyNDIxQzZBOUYxMUUzQjFFNjgyMjcxRTkxRTIzMyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PlnySxAAAADPSURBVHjaxFLBDYMwDLQjMgEf4M8E4Y0YgzHaTTpGFuDfARAdoH3DiwmQSLmqRIZSpPLpSTH4krOjc9g5R0cQIDDzgozj2Ffrum6xOTcKtqolSUJCuNlR0UH8WTiZcpPGzEaB3xVWVXVO0/QhOeTgP1rKOU7/QdM0RZ7nd2OMwxc5eHkeixEm+71aKUVhGJLWmoZhoL7vaRxHX7xtW/ZzlHOTgDiKIirL8oTcWnuZ914dsyzbfXfoCuAmdV3z15ezBgRr8Nuc4ocRXhGeAgwAFHJVgfQ6KdUAAAAASUVORK5CYII=',
        'sFullScreenImage': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QkMwOTc3QTg2QTlGMTFFMzk5MzhBOTM4OEFCMzg3MTciIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QkMwOTc3QTk2QTlGMTFFMzk5MzhBOTM4OEFCMzg3MTciPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpCQzA5NzdBNjZBOUYxMUUzOTkzOEE5Mzg4QUIzODcxNyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpCQzA5NzdBNzZBOUYxMUUzOTkzOEE5Mzg4QUIzODcxNyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PvZjiPIAAAE/SURBVHjanFKxaoRAEHXhbM4UopViGWzv0C5VUvsJqdP7A/6Dfb7DMmCVSuRCunClaKUYSKUQM2+zY1SsMjDuuDtv9u3ME9M0af+xAz6u687opmnENslxnFX1uq6FhhvDMKRluofvVeczuMr9vREmhMhRmRy/F3IukhOjM7Mh4B9VNkqQq67rR9u25VnbtsdxHPkZ6zcWRfFAN2qGYVzx3/e9X1XVC2LLsnzTNK8MQK5kCL4AwcqylPTiOH4m8C1igNI0fUIcBEGu3rwGRlFkK3qvRM9XtD+I9h3iLMvaXaDneRdF78T0cHPXdW+Iif6ZgavmALClB9q0nBRw3RyMAa0mWnJzGIbvJEneOeb9hRgEK0e2mjtG9kX+qeJH8hs163lkh2UlVWAruYIlp8ShzeNYqEQqaE9ym638R4ABADZiqF446UJLAAAAAElFTkSuQmCC'
    }
});

// FIXME child window에서 접근할 수 있도록 global변수로 일단 빼둠. 나중에 리팩토링할 것.
//var selectdTracesBox = {};

pinpointApp.directive('scatter',
    [ 'scatterConfig', '$rootScope', '$timeout', 'webStorage', 'TransactionDao', '$window',
        function (cfg, $rootScope, $timeout, webStorage, oTransactionDao, $window) {
            return {
                template: '<div class="scatter"></div>',
                restrict: 'EA',
                replace: true,
                link: function (scope, element, attrs) {

                    // define private variables
                    var oScatterChart, oNavbarVo;

                    // define private variables of methods
                    var showScatter, makeScatter;

                    // initialize
                    oScatterChart = null;
                    oNavbarVo = null;

                    /**
                     * show scatter
                     * @param applicationName
                     * @param from
                     * @param to
                     * @param period
                     * @param filter
                     */
                    showScatter = function (applicationName, from, to, period, filter) {
                        if (oScatterChart) {
//							oScatterChart.clear();
                        }

//                    selectdTracesBox = {};
//						var fullscreenButton = $("#scatterChartContainer I.icon-fullscreen");
//						fullscreenButton.data("applicationName", applicationName);
//						fullscreenButton.data("from", from);
//						fullscreenButton.data("to", to);
//						fullscreenButton.data("period", period);
//						fullscreenButton.data("usePeriod", usePeriod);
//						fullscreenButton.data("filter", filter);

//						var downloadButton = $("#scatterChartContainer A");

//						var imageFileName = applicationName +
//								"_" +
//								new Date(from).toString("yyyyMMdd_HHmm") +
//								"~" +
//								new Date(to).toString("yyyyMMdd_HHmm") +
//								"_response_scatter.png";
//
//						downloadButton.attr("download", imageFileName);
//						downloadButton.unbind("click");
//						downloadButton.bind("click", function() {
//							var sImageUrl = oScatterChart.getChartAsPNG();
//							$(this).attr('href', sImageUrl);
//						});

//						$("#scatterChartContainer SPAN").unbind("click");
//						$("#scatterChartContainer SPAN").bind("click", function() {
//							showRequests(applicationName, from, to, period, usePeriod, filter);
//						});

//						$("#scatterChartContainer").show();

                        var bDrawOnceAll = false;

                        var htDataSource = {
                            sUrl: function (nFetchIndex) {
                                return cfg.get.scatterData;
                            },
                            htParam: function (nFetchIndex, htLastFetchParam, htLastFetchedData) {
                                // calculate parameter
                                var htData;
                                if (nFetchIndex === 0) {
                                    htData = {
                                        'application': applicationName,
                                        'from': from,
                                        'to': to,
                                        'limit': cfg.nFetchLimit,
                                        'v': 2
                                    };
                                } else {
                                    htData = {
                                        'application': applicationName,
                                        // array[0] 이 최근 값, array[len]이 오래된 이다.
                                        'from': from,
                                        'to': htLastFetchedData.resultFrom - 1,
                                        'limit': cfg.nFetchLimit,
                                        'v': 2
                                    };
                                }
                                if (filter) {
                                    htData.filter = filter;
                                }

                                return htData;
                            },
                            nFetch: function (htLastFetchParam, htLastFetchedData) {
                                // -1 : stop, n = 0 : immediately, n > 0 : interval
                                if (htLastFetchedData.resultFrom - 1 > from) {
                                    if (cfg.useIntervalForFetching) {
                                        bDrawOnceAll = true;
                                        return cfg.nFetchingInterval;
                                    }
                                    // TO THE NEXT
                                    return 0;
                                }

                                // STOP
                                return -1;
                            },
                            htOption: {
                                dataType: 'jsonp',
                                jsonp: '_callback'
                            },
                            index: {
                                x: 0,
                                y: 1,
                                transactionId: 2,
                                type: 3
                            },
                            type: {
                                '0': 'Failed',
                                '1': 'Success'
                            }
                        };
                        oScatterChart.drawWithDataSource(htDataSource);
                    };

                    /**
                     * make scatter
                     * @param title
                     * @param start
                     * @param end
                     * @param period
                     * @param filter
                     * @param w
                     * @param h
                     */
                    makeScatter = function (title, start, end, period, filter, w, h) {
                        if (!Modernizr.canvas) {
                            alert("Can't draw scatter. Not supported browser.");
                        }

                        var options = angular.copy(cfg.options);
                        options.sContainerId = element;
                        options.nWidth = w ? w : 400;
                        options.nHeight = h ? h : 250;
                        options.nXMin = start;
                        options.nXMax = end;
                        options.sTitle = title;
                        options.fOnSelect = function (htPosition, htXY) {
                            var transactions = {
                                htXY: htXY,
                                aTraces: []
                            };
                            transactions.aTraces = this.getDataByXY(htXY.nXFrom, htXY.nXTo, htXY.nYFrom, htXY.nYTo);
                            if (transactions.aTraces.length === 0) {
                                return;
                            }

                            var token = 'transactionsFromScatter_' + _.random(100000, 999999);
//                            webStorage.session.add(token, transactions);
//                            window[token] = transactions;
//                            window.open("/selectedScatter.pinpoint", token);

                            oTransactionDao.addData(token, transactions);
                            $window.open('#/transactionList', token);
                        };
                        options.fFullScreenMode = function () {
                            var url = '#/scatterFullScreenMode/' + oNavbarVo.getApplication() + '/' + oNavbarVo.getPeriod() + '/' + oNavbarVo.getQueryEndTime();
                            if (oNavbarVo.getFilter()) {
                                url += '/' + oNavbarVo.getFilter();
                            }
                            $window.open(url, "width=900, height=700, resizable=yes");
                        };

                        $timeout(function () {
                            if (oScatterChart !== null) {
                                oScatterChart.destroy();
                            }
                            oScatterChart = new BigScatterChart(options);
                            showScatter(title, start, end, period, filter);
                        }, 100);

                    };

                    /**
                     * scope event on scatter.initialize
                     */
                    scope.$on('scatter.initialize', function (event, navbarVo, width, height) {
                        oNavbarVo = navbarVo;
                        makeScatter(oNavbarVo.getApplicationName(), oNavbarVo.getQueryStartTime(), oNavbarVo.getQueryEndTime(), oNavbarVo.getQueryPeriod(), oNavbarVo.getFilter(), width, height);
                    });

                    /**
                     * scope event on scatter.initializeWithNode
                     */
                    scope.$on('scatter.initializeWithNode', function (event, node) {
                        makeScatter(node.applicationName || node.text, oNavbarVo.getQueryStartTime(), oNavbarVo.getQueryEndTime(), oNavbarVo.getQueryPeriod(), oNavbarVo.getFilter());
                    });

                }
            };
        } ]);
