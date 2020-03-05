// TODO: Restructure it according to the new stylesheet
export default {
    general: {
        common: {
            funcServerMapImagePath: null,
            icon: {
                error: 'ERROR_s',
                filter: 'FILTER'
            },
            font: {
                small: '8pt avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif',
                normal: '10pt Helvetica, Arial, avn85,NanumGothic,ng,dotum,AppleGothic,sans-serif',
                big: 'bold 12pt Helvetica, Arial, avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif'
            }
        },
        circle: {
            default: {
                stroke: '#D0D7DF',
                strokeWidth: 5
            },
            good: {
                stroke: '#32BA94',
                strokeWidth: 5,
            },
            slow: {
                stroke: '#E48022',
                strokeWidth: 5,
            },
            bad: {
                stroke: '#F0515B',
                strokeWidth: 5,
            }
        },
        instance: {
            shape: {
                fill: '#90A1AB',
                stroke: '#90A1AB',
                strokeWidth: 1
            },
            text: {
                stroke: '#000'
            }
        },
        node: {
            main: {
                border: {
                    stroke: '#FFF',
                    strokeWidth: 0
                },
                stroke: '#D0D7DF',
                strokeWidth: 1,
                fill: {
                    top: '#F3F4F6',
                    bottom: '#FFF'
                },
                text: {
                    stroke: '#000'
                }
            },
            normal: {
                border: {
                    stroke: '#DDD',
                    strokeWidth: 4
                },
                stroke: '#D0D7DF',
                strokeWidth: 1,
                fill: {
                    top: '#FFF',
                    bottom: '#F3F4F6'
                },
                text: {
                    stroke: '#000'
                }
            },
            highlight: {
                border: {
                    stroke: '#4A61D1',
                    strokeWidth: 4
                }
            }
        },
        link: {
            normal: {
                line: {
                    stroke: '#C0C3C8',
                    strokeWidth: 1.5
                },
                arrow: {
                    fill: '#C0C3C8',
                    stroke: '#C0C3C8',
                },
                textBox: {
                    fill: '#FFF',
                    stroke: 'transparent'
                },
                fontColor: {
                    normal: '#000',
                    alert: '#FF1300'
                },
                fontFamily: '10pt avn85,NanumGothic,ng,dotum,AppleGothic,sans-serif'
            },
            highlight: {
                line: {
                    stroke: '#4763D0',
                    strokeWidth: 1.5
                },
                arrow: {
                    fill: '#4763D0',
                    stroke: '#4763D0',
                },
                textBox: {
                    fill: '#EDF2F8',
                    stroke: '#EDF2F8'
                },
                fontColor: {
                    normal: '#000',
                    alert: '#FF1300'
                },
                fontFamily: 'bold 12pt avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif'
            }
        }
    }
};
