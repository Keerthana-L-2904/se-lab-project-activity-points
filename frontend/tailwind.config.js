 tailwind.config.js
module.exports = {
    theme: {
      extend: {
        animation: {
          'fade-in': 'fadeIn 0.5s ease-in forwards',
          'slide-up': 'slideUp 0.5s ease-out forwards',
        },
        keyframes: {
          fadeIn: {
            '0%': { opacity: 0 },
            '100%': { opacity: 1 },
          },
          slideUp: {
            '0%': { transform: 'translateY(10px)', opacity: 0 },
            '100%': { transform: 'translateY(0)', opacity: 1 },
          },
        },
      },
    },
    plugins: [],
  };
  